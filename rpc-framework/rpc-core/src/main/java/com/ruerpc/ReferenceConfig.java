package com.ruerpc;

import com.ruerpc.discovery.Registry;
import com.ruerpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author Rue
 * @date 2025/5/20 13:48
 */
@Slf4j
public class ReferenceConfig<T> {



    private Class<T> interfaceRef;



    private Registry registry;

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

    /**
     * 代理设计模式，生成一个api接口的代理对象
     * @return 代理对象
     */
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //1. 从注册中心发现可用服务
                //传入服务的名字，返回ip+端口
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if(log.isDebugEnabled()) {
                    log.debug("服务调用方，发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
                }
                return null;
            }
        });
        return (T)helloProxy;
    }

}
