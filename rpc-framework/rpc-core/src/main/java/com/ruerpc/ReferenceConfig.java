package com.ruerpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Rue
 * @date 2025/5/20 13:48
 */
public class ReferenceConfig<T> {

    private Class<T> instance;

    public Class<T> getInstance() {
        return instance;
    }

    public void setInstance(Class<T> instance) {
        this.instance = instance;
    }


    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{instance};
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("hello proxy");
                return null;
            }
        });
        return (T)helloProxy;
    }
}
