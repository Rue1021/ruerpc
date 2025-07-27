package com.ruerpc;

import com.ruerpc.core.HeartbeatDetector;
import com.ruerpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rue
 * @date 2025/5/20 12:39
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        //泛型指定了要调用的远程服务接口类型
        ReferenceConfig<Hello> reference = new ReferenceConfig<>();

        reference.setInterfaceRef(Hello.class);

        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .compress("gzip")
                .group("primary")
                .reference(reference);

        //获取Hello接口服务的动态代理对象，后续对Hello的调用会被拦截并转为远程调用
        Hello dynamicProxy = reference.get();


            //测试
            for (int i = 0; i < 2; i++) {
                dynamicProxy.sayHi("ruerpc")
                        .thenAccept(result -> System.out.println(result))
                        .exceptionally(ex -> {
                            log.error("调用失败", ex);
                            return null;
                        });
//                String sayHi = dynamicProxy.sayHi("ruerpc");
//                log.info("sayHi -> {}, port:{}", sayHi, RueRPCBootstrap.getInstance()
//                        .getConfiguration().getPort());
            }

    }
}
