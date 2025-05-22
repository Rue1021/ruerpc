package com.ruerpc;

/**
 * @author Rue
 * @date 2025/5/20 12:39
 */
public class Application {
    public static void main(String[] args) {

        ReferenceConfig<Hello> reference = new ReferenceConfig<>();
        reference.setInstance(Hello.class);

        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        Hello hello = reference.get();
        hello.sayHi("ruerpc");
    }
}
