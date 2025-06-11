package com.ruerpc.loadbalancer;

import java.net.InetSocketAddress;

/**
 * @author Rue
 * @date 2025/6/11 14:01
 *
 * 负载均衡器接口
 */
public interface LoadBalancer {

    /**
     * 根据服务名称，找到一个可用服务
     * @param serviceName
     * @return
     */
    InetSocketAddress selectServiceAddress(String serviceName);
}
