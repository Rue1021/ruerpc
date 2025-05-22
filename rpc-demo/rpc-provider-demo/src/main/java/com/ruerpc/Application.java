package com.ruerpc;

import com.ruerpc.Impl.HelloImpl;

/**
 * @author Rue
 * @date 2025/5/20 12:35
 */
public class Application {
    public static void main(String[] args) {

        ServiceConfig<Hello> service = new ServiceConfig<>();
        service.setInterface(Hello.class);
        service.setRef(new HelloImpl());


        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                .publish(service)
                .start();
    }
}
