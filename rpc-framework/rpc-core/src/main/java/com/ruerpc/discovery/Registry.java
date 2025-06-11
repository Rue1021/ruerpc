package com.ruerpc.discovery;

import com.ruerpc.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Rue
 * @date 2025/5/22 18:56
 */
public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务名称
     * @return 服务的ip+端口
     */
    List<InetSocketAddress> lookup(String serviceName);
}
