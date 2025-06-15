package com.ruerpc;

import com.ruerpc.discovery.RegistryConfig;
import com.ruerpc.impl.HelloImpl;

/**
 * @author Rue
 * @date 2025/5/20 12:35
 */
public class ProviderApplication {
    public static void main(String[] args) {

        ServiceConfig<Hello> service = new ServiceConfig<>();
        service.setInterface(Hello.class);
        service.setRef(new HelloImpl());


        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                //发布服务
                //.publish(service)
                //扫包批量发布
                .scan("com.ruerpc")
                .start();
    }
}
