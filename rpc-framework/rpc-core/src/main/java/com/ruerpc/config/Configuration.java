package com.ruerpc.config;

import com.ruerpc.IdGenerator;
import com.ruerpc.ProtocolConfig;
import com.ruerpc.compress.Compressor;
import com.ruerpc.compress.impl.GzipCompressor;
import com.ruerpc.discovery.RegistryConfig;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.ruerpc.protection.CircuitBreaker;
import com.ruerpc.protection.RateLimiter;
import com.ruerpc.serialize.Serializer;
import com.ruerpc.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/15 19:42
 * <p>
 * 全局的配置类，优先级：代码配置 --> xml配置 --> spi配置（主动发现）--> 默认项
 */
@Data
@Slf4j
public class Configuration {

    //配置信息 --> 端口号
    private int port = 8090;

    //配置信息 --> 应用程序的名字
    private String appName = "default";

    //配置信息 --> 注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    //配置信息 --> 序列化协议
    private String serializeType = "jdk";

    //配置信息 --> 压缩的协议
    private String compressType = "gzip";

    //配置信息 --> id发号器
    private IdGenerator idGenerator = new IdGenerator(1L, 2L);

    //配置信息 --> 负载均衡策略
    private LoadBalancer loadBalancer = new ConsistentHashLoadBalancer();

    //为每一个ip配置一个限流器
    private Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    //为每一个ip配置一个熔断器
    private Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);

    public Configuration() {
        //1. 成员变量的默认配置项

        //2.spi自动发现机制相关配置
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        //读取xml获取上面的信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);
    }

}