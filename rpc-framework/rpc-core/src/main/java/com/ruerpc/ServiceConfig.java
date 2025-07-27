package com.ruerpc;

/**
 * @author Rue
 * @date 2025/5/20 13:39
 */
public class ServiceConfig<T> {

    private Class<?> interfaceProvider;
    private Object ref; //具体的引用的实现
    private String group = "default"; //分组节点

    public Class<?> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }


    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
