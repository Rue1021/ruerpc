package com.ruerpc.discovery;

import com.ruerpc.Constant;
import com.ruerpc.discovery.impl.ZooKeeperRegistry;
import com.ruerpc.exceptions.DiscoveryException;

/**
 * @author Rue
 * @date 2025/5/20 13:03
 */
public class RegistryConfig {

    //定义连接的url，我们的注册中心是zookeeper://127.0.0.1:2181
    private String connectionString;

    public RegistryConfig(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * 获取注册中心
     * @return 具体的注册中心实例
     */
    public Registry getRegistry() {
        //获取注册中心的类型，由于私有方法getRegistryType里做了处理，所以这里不会出现空指针
        //1. 得到://的前半部分，目前为zookeeper，后面也可以用其他的
        String registryType = getRegistryType(connectionString, true).toLowerCase().trim();
        //2. 目前我们传入的是zookeeper
        if (registryType.equals("zookeeper")) {
            //3. 得到://的后半部分，即ip:端口
            String host = getRegistryType(connectionString, false);
            //4. 返回一个ZooKeeperRegistry对象
            return new ZooKeeperRegistry(host, Constant.TIMEOUT);
        }
        throw new DiscoveryException("未发现合适的注册中心");
    }

    private String getRegistryType(String connectionString, boolean isType) {
        String[] typeAndHost = connectionString.split("://");
        if (typeAndHost.length != 2) {
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        if (isType) {
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }
}
