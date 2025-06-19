package com.ruerpc;

import com.ruerpc.discovery.Registry;
import com.ruerpc.proxy.handler.RPCConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author Rue
 * @date 2025/5/20 13:48
 */
@Slf4j
public class ReferenceConfig<T> {


    private Class<T> interfaceRef;

    private Registry registry;
    private String group;

    /**
     * 代理设计模式，生成一个api接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Class<T>[] classes = new Class[]{interfaceRef};

        InvocationHandler handler = new RPCConsumerInvocationHandler(registry, interfaceRef, group);

        //使用动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);

        return (T) helloProxy;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}