package com.ruerpc;

import com.ruerpc.discovery.RegistryConfig;

/**
 * @author Rue
 * @date 2025/5/20 12:39
 */
public class ConsumerApplication {
    public static void main(String[] args) {
        //reference来自远端的引用
        ReferenceConfig<Hello> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(Hello.class);

        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        Hello hello = reference.get();
        hello.sayHi("ruerpc");
    }
}
