package com.ruerpc;

import com.ruerpc.discovery.Registry;
import com.ruerpc.proxy.handler.RPCConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author Rue
 * @date 2025/5/20 13:48
 *
 * 服务引用配置类
 * 1. 封装消费者引用远程服务所需的所有配置参数 2.生成服务接口的代理对象，将对本地接口的调用转换为远程调用
 */
@Slf4j
public class ReferenceConfig<T> {

    //远端服务接口类对象，用于生成正确的代理对象
    private Class<T> interfaceRef;

    //注册中心
    private Registry registry;

    //服务分组，实现环境隔离
    private String group;

    /**
     * 代理设计模式，生成一个远端接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {
        //获取当前线程的上下文类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //创建包含服务接口的Class数组
        Class<T>[] classes = new Class[]{interfaceRef};
        //实例化RPCConsumerInvocationHandler，作为调用处理器，这个处理器实际处理远程调用逻辑（序列化、网络通信等）
        InvocationHandler handler = new RPCConsumerInvocationHandler(registry, interfaceRef, group);

        //生成动态代理：使用JDK动态代理创建代理对象，所有对接口方法的调用都会被转发到 InvocationHandler
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        //将代理对象转换为接口类型返回
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