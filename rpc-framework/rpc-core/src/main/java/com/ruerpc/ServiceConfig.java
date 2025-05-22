package com.ruerpc;

/**
 * @author Rue
 * @date 2025/5/20 13:39
 */
public class ServiceConfig<T> {

    private Class<T> interfaceProvider;
    private Object ref;

    public Class<T> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }




}
