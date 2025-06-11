package com.ruerpc;

import com.ruerpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rue
 * @date 2025/5/20 12:39
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        //reference来自远端的引用
        ReferenceConfig<Hello> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(Hello.class);

        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .compress("gzip")
                .reference(reference);

        Hello hello = reference.get();

        String sayHi = hello.sayHi("ruerpc");
        log.info("sayHi -> {}, port:{}", sayHi, RueRPCBootstrap.PORT);

    }
}
