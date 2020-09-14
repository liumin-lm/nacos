/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.config.remote.request.ConfigPublishRequest;
import com.alibaba.nacos.api.config.remote.response.ConfigPubishResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.common.ActionTypes;
import com.alibaba.nacos.common.utils.MapUtils;
import com.alibaba.nacos.config.server.auth.ConfigResourceParser;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.event.ConfigDataChangeEvent;
import com.alibaba.nacos.config.server.service.AggrWhitelist;
import com.alibaba.nacos.config.server.service.ConfigChangePublisher;
import com.alibaba.nacos.config.server.service.repository.PersistService;
import com.alibaba.nacos.config.server.service.trace.ConfigTraceService;
import com.alibaba.nacos.config.server.utils.ParamUtils;
import com.alibaba.nacos.config.server.utils.TimeUtils;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.core.utils.InetUtils;
import com.alibaba.nacos.core.utils.Loggers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * request handler to publish config.
 *
 * @author liuzunfei
 * @version $Id: ConfigPublishRequestHandler.java, v 0.1 2020年07月16日 4:41 PM liuzunfei Exp $
 */
@Component
public class ConfigPublishRequestHandler extends RequestHandler<ConfigPublishRequest, ConfigPubishResponse> {
    
    @Autowired
    private PersistService persistService;
    
    @Override
    @Secured(action = ActionTypes.WRITE, resource = "", parser = ConfigResourceParser.class)
    public ConfigPubishResponse handle(ConfigPublishRequest myRequest, RequestMeta meta) throws NacosException {
        
        try {
            String dataId = myRequest.getDataId();
            String group = myRequest.getGroup();
            String content = myRequest.getContent();
            final String tenant = myRequest.getTenant();
            
            final String srcIp = meta.getClientIp();
            final String requestIpApp = myRequest.getAdditionParam("requestIpApp");
            final String tag = myRequest.getAdditionParam("tag");
            final String appName = myRequest.getAdditionParam("appName");
            final String type = myRequest.getAdditionParam("type");
            final String srcUser = myRequest.getAdditionParam("src_user");
            
            // check tenant
            ParamUtils.checkParam(dataId, group, "datumId", content);
            ParamUtils.checkParam(tag);
            Map<String, Object> configAdvanceInfo = new HashMap<String, Object>(10);
            MapUtils.putIfValNoNull(configAdvanceInfo, "config_tags", myRequest.getAdditionParam("configTags"));
            MapUtils.putIfValNoNull(configAdvanceInfo, "desc", myRequest.getAdditionParam("desc"));
            MapUtils.putIfValNoNull(configAdvanceInfo, "use", myRequest.getAdditionParam("use"));
            MapUtils.putIfValNoNull(configAdvanceInfo, "effect", myRequest.getAdditionParam("effect"));
            MapUtils.putIfValNoNull(configAdvanceInfo, "type", type);
            MapUtils.putIfValNoNull(configAdvanceInfo, "schema", myRequest.getAdditionParam("schema"));
            ParamUtils.checkParam(configAdvanceInfo);
            
            if (AggrWhitelist.isAggrDataId(dataId)) {
                Loggers.RPC_DIGEST
                        .warn("[aggr-conflict] {} attemp to publish single data, {}, {}", srcIp, dataId, group);
                throw new NacosException(NacosException.NO_RIGHT, "dataId:" + dataId + " is aggr");
            }
            
            final Timestamp time = TimeUtils.getCurrentTime();
            String betaIps = myRequest.getAdditionParam("betaIps");
            ConfigInfo configInfo = new ConfigInfo(dataId, group, tenant, appName, content);
            configInfo.setType(type);
            if (StringUtils.isBlank(betaIps)) {
                if (StringUtils.isBlank(tag)) {
                    persistService.insertOrUpdate(srcIp, srcUser, configInfo, time, configAdvanceInfo, true);
                    ConfigChangePublisher.notifyConfigChange(
                            new ConfigDataChangeEvent(false, dataId, group, tenant, time.getTime()));
                } else {
                    persistService.insertOrUpdateTag(configInfo, tag, srcIp, srcUser, time, true);
                    ConfigChangePublisher.notifyConfigChange(
                            new ConfigDataChangeEvent(false, dataId, group, tenant, tag, time.getTime()));
                }
            } else {
                // beta publish
                persistService.insertOrUpdateBeta(configInfo, betaIps, srcIp, srcUser, time, true);
                ConfigChangePublisher
                        .notifyConfigChange(new ConfigDataChangeEvent(true, dataId, group, tenant, time.getTime()));
            }
            ConfigTraceService
                    .logPersistenceEvent(dataId, group, tenant, requestIpApp, time.getTime(), InetUtils.getSelfIp(),
                            ConfigTraceService.PERSISTENCE_EVENT_PUB, content);
            return ConfigPubishResponse.buildSuccessResponse();
        } catch (Exception e) {
            Loggers.RPC_DIGEST.error("[ConfigPublishRequestHandler] publish config error ,request ={}", myRequest);
            return ConfigPubishResponse.buildFailResponse(e.getMessage());
        }
    }
    
}
