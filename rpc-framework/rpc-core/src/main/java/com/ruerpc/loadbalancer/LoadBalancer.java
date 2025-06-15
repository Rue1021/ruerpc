package com.ruerpc.loadbalancer;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.util.List;

/**
 * @author Rue
 * @date 2025/6/11 14:01
 *
 * 负载均衡器接口
 * 我们做的是客户端负载均衡
 */
public interface LoadBalancer {

    /**
     * 根据服务名称，找到一个可用服务
     * @param serviceName
     * @return
     */
    InetSocketAddress selectServiceAddress(String serviceName);

    /**
     * 感知到节点动态上下线时，需要重新进行负载均衡
     * @param serviceName 服务名称
     * @param addresses 重新拉取的服务列表
     */
    void reLoadBalance(String serviceName, List<InetSocketAddress> addresses);
}
