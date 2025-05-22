package com.ruerpc;

/**
 * @author Rue
 * @date 2025/5/20 14:46
 */
public class Constant {
    //zookeeper的默认连接地址
    public static final String DEFAULT_ZK_CONNECT_ADDR = "127.0.0.1:2181";

    //zookeeper默认的超时时间
    public static final int TIMEOUT = 10000;

    //服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDER_PATH = "/ruerpc-metadata/providers";
    public static final String BASE_CONSUMER_PATH = "/ruerpc-metadata/consumers";
}
