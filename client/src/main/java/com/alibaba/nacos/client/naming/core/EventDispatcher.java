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
package com.alibaba.nacos.client.naming.core;

import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * @author xuanyin
 */
public class EventDispatcher {

    private ExecutorService executor = null;

    private BlockingQueue<ServiceInfo> changedServices = new LinkedBlockingQueue<ServiceInfo>();

    private ConcurrentMap<String, List<EventListener>> observerMap
        = new ConcurrentHashMap<String, List<EventListener>>();

    public EventDispatcher() {

        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "com.alibaba.nacos.naming.client.listener");
                thread.setDaemon(true);

                return thread;
            }
        });

        /**
         * 事件通知
         */
        executor.execute(new Notifier());
    }

    /**
     * 触发监听
     * @param serviceInfo
     * @param clusters
     * @param listener
     */
    public void addListener(ServiceInfo serviceInfo, String clusters, EventListener listener) {

        NAMING_LOGGER.info("[LISTENER] adding " + serviceInfo.getName() + " with " + clusters + " to listener map");
        /**
         * 集合
         */
        List<EventListener> observers = Collections.synchronizedList(new ArrayList<EventListener>());
        observers.add(listener);

        /**
         * 存入缓存
         */
        observers = observerMap.putIfAbsent(ServiceInfo.getKey(serviceInfo.getName(), clusters), observers);
        if (observers != null) {
            observers.add(listener);
        }

        /**
         * 触发监听
         */
        serviceChanged(serviceInfo);
    }

    /**
     * 取消监听
     * @param serviceName
     * @param clusters
     * @param listener
     */
    public void removeListener(String serviceName, String clusters, EventListener listener) {

        NAMING_LOGGER.info("[LISTENER] removing " + serviceName + " with " + clusters + " from listener map");

        /**
         * 获取key对应得所有监听
         */
        List<EventListener> observers = observerMap.get(ServiceInfo.getKey(serviceName, clusters));
        if (observers != null) {
            Iterator<EventListener> iter = observers.iterator();
            while (iter.hasNext()) {
                EventListener oldListener = iter.next();
                /**
                 * 移除对应得EventListener
                 */
                if (oldListener.equals(listener)) {
                    iter.remove();
                }
            }

            /**
             * 监听器全部取消   则移除缓存中得任务
             */
            if (observers.isEmpty()) {
                observerMap.remove(ServiceInfo.getKey(serviceName, clusters));
            }
        }
    }

    public List<ServiceInfo> getSubscribeServices() {
        List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();
        for (String key : observerMap.keySet()) {
            serviceInfos.add(ServiceInfo.fromKey(key));
        }
        return serviceInfos;
    }

    /**
     * 通知监听器   新增监听任务
     * @param serviceInfo
     */
    public void serviceChanged(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return;
        }

        changedServices.add(serviceInfo);
    }

    /**
     * 事件通知
     */
    private class Notifier implements Runnable {
        @Override
        public void run() {
            while (true) {
                ServiceInfo serviceInfo = null;
                try {
                    /**
                     * 存入BlockingQueue
                     */
                    serviceInfo = changedServices.poll(5, TimeUnit.MINUTES);
                } catch (Exception ignore) {
                }

                if (serviceInfo == null) {
                    continue;
                }

                try {
                    /**
                     * 获取监听器
                     */
                    List<EventListener> listeners = observerMap.get(serviceInfo.getKey());

                    if (!CollectionUtils.isEmpty(listeners)) {
                        for (EventListener listener : listeners) {
                            List<Instance> hosts = Collections.unmodifiableList(serviceInfo.getHosts());
                            /**
                             * 事件通知   由用户来实现业务逻辑  例如：NamingExample
                             */
                            listener.onEvent(new NamingEvent(serviceInfo.getName(), serviceInfo.getGroupName(), serviceInfo.getClusters(), hosts));
                        }
                    }

                } catch (Exception e) {
                    NAMING_LOGGER.error("[NA] notify error for service: "
                        + serviceInfo.getName() + ", clusters: " + serviceInfo.getClusters(), e);
                }
            }
        }
    }

    public void setExecutor(ExecutorService executor) {
        ExecutorService oldExecutor = this.executor;
        this.executor = executor;

        oldExecutor.shutdown();
    }
}
