/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.remote;

import com.alibaba.nacos.api.config.remote.request.ConfigQueryRequest;
import com.alibaba.nacos.api.config.remote.response.ConfigQueryResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.common.ActionTypes;
import com.alibaba.nacos.config.server.auth.ConfigResourceParser;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.model.CacheItem;
import com.alibaba.nacos.config.server.model.ConfigInfoBase;
import com.alibaba.nacos.config.server.service.ConfigCacheService;
import com.alibaba.nacos.config.server.service.repository.PersistService;
import com.alibaba.nacos.config.server.service.trace.ConfigTraceService;
import com.alibaba.nacos.config.server.utils.DiskUtil;
import com.alibaba.nacos.config.server.utils.GroupKey2;
import com.alibaba.nacos.config.server.utils.LogUtil;
import com.alibaba.nacos.config.server.utils.PropertyUtil;
import com.alibaba.nacos.config.server.utils.TimeUtils;
import com.alibaba.nacos.core.remote.RequestHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.alibaba.nacos.config.server.utils.LogUtil.PULL_LOG;
import static com.alibaba.nacos.config.server.utils.RequestUtil.CLIENT_APPNAME_HEADER;

/**
 * ConfigQueryRequestHandler.
 *
 * @author liuzunfei
 * @version $Id: ConfigQueryRequestHandler.java, v 0.1 2020年07月14日 9:54 AM liuzunfei Exp $
 */
@Component
public class ConfigQueryRequestHandler extends RequestHandler<ConfigQueryRequest, ConfigQueryResponse> {
    
    private static final int TRY_GET_LOCK_TIMES = 9;
    
    private final PersistService persistService;
    
    public ConfigQueryRequestHandler(PersistService persistService) {
        this.persistService = persistService;
    }
    
    @Override
    @Secured(action = ActionTypes.READ, parser = ConfigResourceParser.class)
    public ConfigQueryResponse handle(ConfigQueryRequest request, RequestMeta requestMeta) throws NacosException {
        ConfigQueryRequest configQueryRequest = (ConfigQueryRequest) request;
        
        String group = configQueryRequest.getGroup();
        String dataId = configQueryRequest.getDataId();
        String tenant = configQueryRequest.getTenant();
        String clientIp = requestMeta.getClientIp();
        try {
            ConfigQueryResponse context = getContext(dataId, group, tenant, configQueryRequest.getTag(),
                    requestMeta.getClientIp(), requestMeta, request.isNotify());
            return context;
        } catch (Exception e) {
            ConfigQueryResponse contextFail = ConfigQueryResponse
                    .buildFailResponse(ResponseCode.FAIL.getCode(), e.getMessage());
            return contextFail;
        }
        
    }
    
    private ConfigQueryResponse getContext(String dataId, String group, String tenant, String tag, String clientIp,
            RequestMeta meta, boolean notify) throws UnsupportedEncodingException {
        
        ConfigQueryResponse response = new ConfigQueryResponse();
    
        final String groupKey = GroupKey2.getKey(dataId, group, tenant);
        
        String autoTag = meta.getLabels().get("Vipserver-Tag");
        
        String requestIpApp = meta.getLabels().get(CLIENT_APPNAME_HEADER);
        
        int lockResult = tryConfigReadLock(groupKey);
        
        boolean isBeta = false;
        if (lockResult > 0) {
            //FileInputStream fis = null;
            try {
                String md5 = Constants.NULL;
                long lastModified = 0L;
                CacheItem cacheItem = ConfigCacheService.getContentCache(groupKey);
                if (cacheItem != null) {
                    if (cacheItem.isBeta()) {
                        if (cacheItem.getIps4Beta().contains(clientIp)) {
                            isBeta = true;
                        }
                    }
                    String configType = cacheItem.getType();
                    response.setContentType((null != configType) ? configType : "text");
                }
                File file = null;
                ConfigInfoBase configInfoBase = null;
                PrintWriter out = null;
                if (isBeta) {
                    md5 = cacheItem.getMd54Beta();
                    lastModified = cacheItem.getLastModifiedTs4Beta();
                    if (PropertyUtil.isDirectRead()) {
                        configInfoBase = persistService.findConfigInfo4Beta(dataId, group, tenant);
                    } else {
                        file = DiskUtil.targetBetaFile(dataId, group, tenant);
                    }
                    response.addLabel("isBeta", "Y");
                } else {
                    if (StringUtils.isBlank(tag)) {
                        if (isUseTag(cacheItem, autoTag)) {
                            if (cacheItem != null) {
                                if (cacheItem.tagMd5 != null) {
                                    md5 = cacheItem.tagMd5.get(autoTag);
                                }
                                if (cacheItem.tagLastModifiedTs != null) {
                                    lastModified = cacheItem.tagLastModifiedTs.get(autoTag);
                                }
                            }
                            if (PropertyUtil.isDirectRead()) {
                                configInfoBase = persistService.findConfigInfo4Tag(dataId, group, tenant, autoTag);
                            } else {
                                file = DiskUtil.targetTagFile(dataId, group, tenant, autoTag);
                            }
                            
                            response.addLabel("Vipserver-Tag",
                                    URLEncoder.encode(autoTag, StandardCharsets.UTF_8.displayName()));
                        } else {
                            md5 = cacheItem.getMd5();
                            lastModified = cacheItem.getLastModifiedTs();
                            if (PropertyUtil.isDirectRead()) {
                                configInfoBase = persistService.findConfigInfo(dataId, group, tenant);
                            } else {
                                file = DiskUtil.targetFile(dataId, group, tenant);
                            }
                            if (configInfoBase == null && fileNotExist(file)) {
                                // FIXME CacheItem
                                // No longer exists. It is impossible to simply calculate the push delayed. Here, simply record it as - 1.
                                ConfigTraceService.logPullEvent(dataId, group, tenant, requestIpApp, -1,
                                        ConfigTraceService.PULL_EVENT_NOTFOUND, -1, clientIp, false);
                                
                                // pullLog.info("[client-get] clientIp={}, {},
                                // no data",
                                // new Object[]{clientIp, groupKey});
    
                                response.setErrorInfo(ConfigQueryResponse.CONFIG_NOT_FOUND, "config data not exist");
                                return response;
                            }
                        }
                    } else {
                        if (cacheItem != null) {
                            if (cacheItem.tagMd5 != null) {
                                md5 = cacheItem.tagMd5.get(tag);
                            }
                            if (cacheItem.tagLastModifiedTs != null) {
                                Long lm = cacheItem.tagLastModifiedTs.get(tag);
                                if (lm != null) {
                                    lastModified = lm;
                                }
                            }
                        }
                        if (PropertyUtil.isDirectRead()) {
                            configInfoBase = persistService.findConfigInfo4Tag(dataId, group, tenant, tag);
                        } else {
                            file = DiskUtil.targetTagFile(dataId, group, tenant, tag);
                        }
                        if (configInfoBase == null && fileNotExist(file)) {
                            // FIXME CacheItem
                            // No longer exists. It is impossible to simply calculate the push delayed. Here, simply record it as - 1.
                            ConfigTraceService.logPullEvent(dataId, group, tenant, requestIpApp, -1,
                                    ConfigTraceService.PULL_EVENT_NOTFOUND, -1, clientIp, false);
                            
                            // pullLog.info("[client-get] clientIp={}, {},
                            // no data",
                            // new Object[]{clientIp, groupKey});
    
                            response.setErrorInfo(ConfigQueryResponse.CONFIG_NOT_FOUND, "config data not exist");
                            return response;
                            
                        }
                    }
                }
                
                response.addLabel(Constants.CONTENT_MD5, md5);
                
                if (PropertyUtil.isDirectRead()) {
                    response.addLabel("Last-Modified", String.valueOf(lastModified));
                    response.setContent(configInfoBase.getContent());
                    response.setResultCode(ResponseCode.SUCCESS.getCode());
                    
                } else {
                    //read from file
                    String content = readFileContent(file);
                    response.setContent(content);
                    response.addLabel("Last-Modified", String.valueOf(lastModified));
                    response.setResultCode(ResponseCode.SUCCESS.getCode());
                    
                }
                
                LogUtil.PULL_CHECK_LOG.warn("{}|{}|{}|{}", groupKey, clientIp, md5, TimeUtils.getCurrentTimeStr());
                
                final long delayed = System.currentTimeMillis() - lastModified;
                
                // TODO distinguish pull-get && push-get
                /*
                 Otherwise, delayed cannot be used as the basis of push delay directly,
                 because the delayed value of active get requests is very large.
                 */
                ConfigTraceService.logPullEvent(dataId, group, tenant, requestIpApp, lastModified,
                        ConfigTraceService.PULL_EVENT_OK, delayed, clientIp, notify);
                
            } finally {
                releaseConfigReadLock(groupKey);
            }
        } else if (lockResult == 0) {
            
            // FIXME CacheItem No longer exists. It is impossible to simply calculate the push delayed. Here, simply record it as - 1.
            ConfigTraceService
                    .logPullEvent(dataId, group, tenant, requestIpApp, -1, ConfigTraceService.PULL_EVENT_NOTFOUND, -1,
                            clientIp, notify);
            response.setErrorInfo(ConfigQueryResponse.CONFIG_NOT_FOUND, "config data not exist");
            
        } else {
            PULL_LOG.info("[client-get] clientIp={}, {}, get data during dump", clientIp, groupKey);
            response.setErrorInfo(ConfigQueryResponse.CONFIG_QUERY_CONFLICT,
                    "requested file is being modified, please try later.");
        }
        return response;
    }
    
    /**
     * read content.
     *
     * @param file file to read.
     * @return content.
     */
    public static String readFileContent(File file) {
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(
                    file), Charset.forName(Constants.ENCODE));
            
            reader = new BufferedReader(isr);
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }
    
    private static void releaseConfigReadLock(String groupKey) {
        ConfigCacheService.releaseReadLock(groupKey);
    }
    
    private static boolean fileNotExist(File file) {
        return file == null || !file.exists();
    }
    
    private static int tryConfigReadLock(String groupKey) {
        
        // Lock failed by default.
        int lockResult = -1;
        
        // Try to get lock times, max value: 10;
        for (int i = TRY_GET_LOCK_TIMES; i >= 0; --i) {
            lockResult = ConfigCacheService.tryReadLock(groupKey);
            
            // The data is non-existent.
            if (0 == lockResult) {
                break;
            }
            
            // Success
            if (lockResult > 0) {
                break;
            }
            
            // Retry.
            if (i > 0) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    LogUtil.PULL_CHECK_LOG.error("An Exception occurred while thread sleep", e);
                }
            }
        }
        
        return lockResult;
    }
    
    private static boolean isUseTag(CacheItem cacheItem, String tag) {
        if (cacheItem != null && cacheItem.tagMd5 != null && cacheItem.tagMd5.size() > 0) {
            return StringUtils.isNotBlank(tag) && cacheItem.tagMd5.containsKey(tag);
        }
        return false;
    }
    
}
