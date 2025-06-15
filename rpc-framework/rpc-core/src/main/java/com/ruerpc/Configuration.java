package com.ruerpc;

import com.ruerpc.discovery.Registry;
import com.ruerpc.discovery.RegistryConfig;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.ruerpc.transport.message.RueRPCRequest;
import io.netty.channel.Channel;
import lombok.Data;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/15 19:42
 *
 * 全局的配置类，优先级：代码配置 --> xml配置 --> spi配置（主动发现）--> 默认项
 */
@Data
public class Configuration {

    //配置信息 --> 端口号
    private int port = 8090;

    //配置信息 --> 应用程序的名字
    private String appName = "default";

    //配置信息 --> 注册中心
    private RegistryConfig registryConfig;

    //配置信息 --> 序列化协议
    private ProtocolConfig protocolConfig;

    //配置信息 --> 序列化协议
    private String serializeType = "jdk";

    //配置信息 --> 压缩的协议
    private String compressType = "gzip";

    //配置信息 --> id发号器
    private IdGenerator idGenerator = new IdGenerator(1L, 2L);

    //配置信息 --> 负载均衡策略
    private LoadBalancer loadBalancer = new ConsistentHashLoadBalancer();

    public Configuration() {
    }
}
