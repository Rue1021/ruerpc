package com.ruerpc.loadbalancer;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.discovery.Registry;
import com.ruerpc.exceptions.LoadBalanceException;
import com.ruerpc.loadbalancer.impl.RoundRobinLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rue
 * @date 2025/6/11 15:22
 *
 * 模版方法设计模式, 并在抽象类实现一个reLoadBalance方法
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    //一个服务维护一个ServiceSelector
    private Map<String, ServiceSelector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {

        //优先从cache中获得一个ServiceSelector
        ServiceSelector cachedServiceSelector = cache.get(serviceName);

        if (cachedServiceSelector != null) {
            return cachedServiceSelector.getNext();
        }

        List<InetSocketAddress> serviceList = RueRPCBootstrap.getInstance().getRegistry().lookup(serviceName);
        ServiceSelector serviceSelector = getServiceSelector(serviceList);
        //放入缓存
        cache.put(serviceName, serviceSelector);

        return serviceSelector.getNext();
    }

    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> addresses) {
        //根据新的服务列表生成Selector
        cache.put(serviceName, getServiceSelector(addresses));
    }

    /**
     * 由子类进行扩展
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract ServiceSelector getServiceSelector(List<InetSocketAddress> serviceList);

}
