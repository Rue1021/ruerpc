package com.ruerpc;

import com.ruerpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Rue
 * @date 2025/6/19 13:39
 */
@Component
@Slf4j
public class RueRPCStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("--------------------> ruerpc starting.... <-----------------");
        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                //发布服务
                //.publish(service)
                //扫包批量发布
                .scan("com.ruerpc.impl")
                .start();
    }
}
