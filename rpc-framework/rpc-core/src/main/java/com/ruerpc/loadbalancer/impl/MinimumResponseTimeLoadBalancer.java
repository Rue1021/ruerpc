package com.ruerpc.loadbalancer.impl;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.exceptions.LoadBalanceException;
import com.ruerpc.loadbalancer.AbstractLoadBalancer;
import com.ruerpc.loadbalancer.ServiceSelector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rue
 * @date 2025/6/12 19:58
 *
 * 最短响应时间的负载均衡策略
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected ServiceSelector getServiceSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseSelector();
    }

    private static class MinimumResponseSelector implements ServiceSelector {

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> longChannelEntry = RueRPCBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (longChannelEntry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("选取了响应时间为【{}】ms的服务节点", longChannelEntry.getKey());
                }
            }
            if (longChannelEntry != null) {
                return (InetSocketAddress) longChannelEntry.getValue().remoteAddress();
            }
            //如果请求进入轮询阶段时，treemap中还没有通过心跳检测生成缓存，就从CHANNEL_CACHE中拿一个缓存用
            Channel cachedChannel = (Channel) RueRPCBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress)cachedChannel.remoteAddress();
        }

        @Override
        public void reBalance() {

        }
    }
}
