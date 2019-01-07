package com.alibaba.nacos.client.naming.loadbalancer;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.loadbalancer.LoadBalancer;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.core.EventDispatcher;
import com.alibaba.nacos.client.naming.core.HostReactor;
import com.alibaba.nacos.client.naming.utils.StringUtils;

import java.util.List;

/**
 * Base Load-balancer Abstract Class
 * User defines their own load-balancer
 * by inheriting base {@link BaseLoadBalancer}
 * or implementing LoadBalancer{@link LoadBalancer} interface
 *
 * When implementing your own Load-balancer,
 * you need to understand the two classes {@link ServiceInfo} and {@link Instance}.
 *
 * @author XCXCXCXCX
 */
public abstract class BaseLoadBalancer implements LoadBalancer, EventListener{

    private ServiceInfo serviceInfo;

    protected String key;

    private final HostReactor hostReactor;

    private final EventDispatcher eventDispatcher;

    private final Boolean enableListener;

    protected BaseLoadBalancer(String serviceName,
                               List<String> clusters,
                               final HostReactor hostReactor,
                               final EventDispatcher eventDispatcher,
                               Boolean enableListener) {
        this.serviceInfo = hostReactor.getServiceInfo(serviceName, StringUtils.join(clusters, ","));
        //The key can be used to cache Load-Balancer Bean
        this.key = serviceName + ServiceInfo.SPLITER + clusters.toString();
        this.hostReactor = hostReactor;
        this.eventDispatcher = eventDispatcher;
        this.enableListener = enableListener;
        if(enableListener){
            eventDispatcher.addListener(serviceInfo, StringUtils.join(clusters, ","), this);
        }
    }

    public Instance choose(){
        if(!enableListener){
            fetchNewServiceInfo();
        }
        return doChoose(serviceInfo);
    }

    public String getKey() {
        return key;
    }

    /**
     * User can use ServiceInfo to control, ServiceInfo changed when instances changed
     * @return Instance
     */
    public abstract Instance doChoose(final ServiceInfo serviceInfo);

    protected void fetchNewServiceInfo(){
        this.serviceInfo = hostReactor.getServiceInfo(serviceInfo.getName(), serviceInfo.getClusters());
    }

    /**
     * callback event
     * update cache when instances changed
     * @param event
     */
    @Override
    public void onEvent(Event event) {
        fetchNewServiceInfo();
    }
}
