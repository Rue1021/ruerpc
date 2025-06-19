package com.ruerpc.proxy;

import com.ruerpc.ReferenceConfig;
import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.discovery.RegistryConfig;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/19 19:06
 */
public class ProxyFactory {
    private static Map<Class<?>, Object> PROXY_CACHE = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz) {
        Object bean = PROXY_CACHE.get(clazz);
        if (bean != null) {
            return (T)bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(clazz);

        RueRPCBootstrap.getInstance()
                .application("first-ruerpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .compress("gzip")
                .group("primary")
                .reference(reference);

        T t = reference.get();
        PROXY_CACHE.put(clazz, t);
        return t;
    }
}
