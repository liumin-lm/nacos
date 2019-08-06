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
package com.alibaba.nacos.client.naming.backups;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.cache.ConcurrentDiskUtil;
import com.alibaba.nacos.client.naming.cache.DiskCache;
import com.alibaba.nacos.client.naming.core.HostReactor;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.client.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * @author nkorange
 */
public class FailoverReactor {

    private String failoverDir;

    private HostReactor hostReactor;

    /**
     * 容错
     * @param hostReactor
     * @param cacheDir
     */
    public FailoverReactor(HostReactor hostReactor, String cacheDir) {
        this.hostReactor = hostReactor;
        /**
         * 容错路径  C:\Users\Administrator/nacos/naming/quickStart/failover
         */
        this.failoverDir = cacheDir + "/failover";
        this.init();
    }

    private Map<String, ServiceInfo> serviceMap = new ConcurrentHashMap<String, ServiceInfo>();
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("com.alibaba.nacos.naming.failover");
            return thread;
        }
    });

    private Map<String, String> switchParams = new ConcurrentHashMap<String, String>();
    private static final long DAY_PERIOD_MINUTES = 24 * 60;

    public void init() {

        /**
         * 5000毫秒   将容灾策略从磁盘读取到内存中
         */
        executorService.scheduleWithFixedDelay(new SwitchRefresher(), 0L, 5000L, TimeUnit.MILLISECONDS);

        /**
         * 24小时  每隔24小时，把内存中所有的服务数据，写一遍到磁盘中 failoverDir目录下
         */
        executorService.scheduleWithFixedDelay(new DiskFileWriter(), 30, DAY_PERIOD_MINUTES, TimeUnit.MINUTES);

        // backup file on startup if failover directory is empty.
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    File cacheDir = new File(failoverDir);

                    /**
                     * 检查failoverDir目录是否存在  不存在则创建
                     */
                    if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                        throw new IllegalStateException("failed to create cache dir: " + failoverDir);
                    }

                    /**
                     * 检查failoverDir下的文件
                     */
                    File[] files = cacheDir.listFiles();
                    /**
                     * 为空则
                     */
                    if (files == null || files.length <= 0) {
                        new DiskFileWriter().run();
                    }
                } catch (Throwable e) {
                    NAMING_LOGGER.error("[NA] failed to backup file on startup.", e);
                }

            }
        }, 10000L, TimeUnit.MILLISECONDS);
    }

    public Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    class SwitchRefresher implements Runnable {
        long lastModifiedMillis = 0L;

        @Override
        public void run() {
            try {
                File switchFile = new File(failoverDir + UtilAndComs.FAILOVER_SWITCH);
                if (!switchFile.exists()) {
                    switchParams.put("failover-mode", "false");
                    NAMING_LOGGER.debug("failover switch is not found, " + switchFile.getName());
                    return;
                }

                /**
                 * 获取文件最后一次更改时间
                 */
                long modified = switchFile.lastModified();

                if (lastModifiedMillis < modified) {
                    lastModifiedMillis = modified;
                    /**
                     * 获取文件内容   failoverDir下的 UtilAndComs.FAILOVER_SWITCH
                     */
                    String failover = ConcurrentDiskUtil.getFileContent(failoverDir + UtilAndComs.FAILOVER_SWITCH,
                        Charset.defaultCharset().toString());
                    if (!StringUtils.isEmpty(failover)) {
                        /**
                         * 分割
                         */
                        List<String> lines = Arrays.asList(failover.split(DiskCache.getLineSeparator()));

                        for (String line : lines) {
                            String line1 = line.trim();
                            /**
                             * 开启容灾
                             */
                            if ("1".equals(line1)) {
                                switchParams.put("failover-mode", "true");
                                NAMING_LOGGER.info("failover-mode is on");
                                new FailoverFileReader().run();
                            } else if ("0".equals(line1)) {
                                switchParams.put("failover-mode", "false");
                                NAMING_LOGGER.info("failover-mode is off");
                            }
                        }
                    } else {
                        switchParams.put("failover-mode", "false");
                    }
                }

            } catch (Throwable e) {
                NAMING_LOGGER.error("[NA] failed to read failover switch.", e);
            }
        }
    }

    class FailoverFileReader implements Runnable {

        /**
         * 将容灾策略从硬盘中读取到内存中
         */
        @Override
        public void run() {
            Map<String, ServiceInfo> domMap = new HashMap<String, ServiceInfo>(16);

            BufferedReader reader = null;
            try {

                /**
                 * 查看文件夹
                 */
                File cacheDir = new File(failoverDir);
                if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                    throw new IllegalStateException("failed to create cache dir: " + failoverDir);
                }

                File[] files = cacheDir.listFiles();
                if (files == null) {
                    return;
                }

                /**
                 * 便利文件
                 */
                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }

                    if (file.getName().equals(UtilAndComs.FAILOVER_SWITCH)) {
                        continue;
                    }

                    ServiceInfo dom = new ServiceInfo(file.getName());

                    try {
                        /**
                         * 读取文件内容
                         */
                        String dataString = ConcurrentDiskUtil.getFileContent(file,
                            Charset.defaultCharset().toString());
                        reader = new BufferedReader(new StringReader(dataString));

                        String json;
                        if ((json = reader.readLine()) != null) {
                            try {
                                /**
                                 * 将内容转换为ServiceInfo
                                 */
                                dom = JSON.parseObject(json, ServiceInfo.class);
                            } catch (Exception e) {
                                NAMING_LOGGER.error("[NA] error while parsing cached dom : " + json, e);
                            }
                        }

                    } catch (Exception e) {
                        NAMING_LOGGER.error("[NA] failed to read cache for dom: " + file.getName(), e);
                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (Exception e) {
                            //ignore
                        }
                    }

                    /**
                     * 地址列表不为空  则缓存到内存中
                     */
                    if (!CollectionUtils.isEmpty(dom.getHosts())) {
                        domMap.put(dom.getKey(), dom);
                    }
                }
            } catch (Exception e) {
                NAMING_LOGGER.error("[NA] failed to read cache file", e);
            }

            /**
             * 更新
             */
            if (domMap.size() > 0) {
                serviceMap = domMap;
            }
        }
    }

    class DiskFileWriter extends TimerTask {
        @Override
        public void run() {
            Map<String, ServiceInfo> map = hostReactor.getServiceInfoMap();
            for (Map.Entry<String, ServiceInfo> entry : map.entrySet()) {
                ServiceInfo serviceInfo = entry.getValue();
                /**
                 * 过滤特殊数据
                 */
                if (StringUtils.equals(serviceInfo.getKey(), UtilAndComs.ALL_IPS) || StringUtils.equals(
                    serviceInfo.getName(), UtilAndComs.ENV_LIST_KEY)
                    || StringUtils.equals(serviceInfo.getName(), "00-00---000-ENV_CONFIGS-000---00-00")
                    || StringUtils.equals(serviceInfo.getName(), "vipclient.properties")
                    || StringUtils.equals(serviceInfo.getName(), "00-00---000-ALL_HOSTS-000---00-00")) {
                    continue;
                }

                DiskCache.write(serviceInfo, failoverDir);
            }
        }
    }

    public boolean isFailoverSwitch() {
        return Boolean.parseBoolean(switchParams.get("failover-mode"));
    }

    public ServiceInfo getService(String key) {
        ServiceInfo serviceInfo = serviceMap.get(key);

        if (serviceInfo == null) {
            serviceInfo = new ServiceInfo();
            serviceInfo.setName(key);
        }

        return serviceInfo;
    }
}
