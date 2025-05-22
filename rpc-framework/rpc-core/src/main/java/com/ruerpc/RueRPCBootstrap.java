package com.ruerpc;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author Rue
 * @date 2025/5/20 12:28
 */
@Slf4j
public class RueRPCBootstrap {

    private String appName;
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;

    //没有Slf4j注解时需要用这一句
    //private static final Logger logger = LoggerFactory.getLogger(RueRPCBootstrap.class);

    //RueRPCBootstrap是个单例，即希望每个应用程序只有一个实例，用饿汉式写
    private static final RueRPCBootstrap rueRPCBootstrap = new RueRPCBootstrap();


    private RueRPCBootstrap() {}


    public static RueRPCBootstrap getInstance() {
        return rueRPCBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     * @param appName
     * @return this
     */
    public RueRPCBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public RueRPCBootstrap registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return
     */
    public RueRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前服务使用了{}协议进行序列化", protocolConfig);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {

    }

    /**
     * ---------------------------⬇️服务提供方的相关api---------------------------------
     */

    /**
     * 发布服务：将接口->实现注册到服务中心
     * @param service 封装的需要发布的服务
     * @return
     */
    public RueRPCBootstrap publish(ServiceConfig<?> service) {
        if (log.isDebugEnabled()) {
            log.debug("{}服务被注册", service);
        }
        return this;
    }

    /**
     * 批量发布服务
     * @param service
     * @return
     */
    public RueRPCBootstrap publish(List<?> service) {
        return this;
    }


    /**
     * ---------------------------⬇️服务调用方的相关api---------------------------------
     */

    public RueRPCBootstrap reference(ReferenceConfig<?> reference) {
        return this;
    }
}
