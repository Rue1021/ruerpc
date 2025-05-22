package com.ruerpc.discovery;

import com.ruerpc.ServiceConfig;

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
}
