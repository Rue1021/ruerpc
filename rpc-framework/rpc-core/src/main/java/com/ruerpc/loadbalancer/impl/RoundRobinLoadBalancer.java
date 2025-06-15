package com.ruerpc.loadbalancer.impl;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.discovery.Registry;
import com.ruerpc.exceptions.LoadBalanceException;
import com.ruerpc.loadbalancer.AbstractLoadBalancer;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.loadbalancer.ServiceSelector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rue
 * @date 2025/6/11 13:50
 *
 * 轮询负载均衡
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected ServiceSelector getServiceSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements ServiceSelector {

        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0) {
                log.error("RoundRobin负载均衡时发现的服务列表【{}】为空", serviceList);
                throw new LoadBalanceException();
            }
            InetSocketAddress address = serviceList.get(index.get());
            if (index.get() == serviceList.size() - 1) {
                index.set(0);
            } else {
                index.incrementAndGet();
            }
            return address;
        }

    }
}
