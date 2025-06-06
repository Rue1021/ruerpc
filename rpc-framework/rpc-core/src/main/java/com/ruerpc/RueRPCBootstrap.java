package com.ruerpc;

import com.ruerpc.channelHandler.handler.MethodCallHandler;
import com.ruerpc.channelHandler.handler.RueRPCRequestDecoder;
import com.ruerpc.channelHandler.handler.RueRPCResponseEncoder;
import com.ruerpc.discovery.Registry;
import com.ruerpc.discovery.RegistryConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Rue
 * @date 2025/5/20 12:28
 */
@Slf4j
public class RueRPCBootstrap {

    //RueRPCBootstrap是个单例，即希望每个应用程序只有一个实例，用饿汉式写
    private static final RueRPCBootstrap rueRPCBootstrap = new RueRPCBootstrap();

    private String appName;
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8090;

    private Registry registry;

    //维护已经发布并暴露的服务列表 key -> interface的全限定名 value -> ServiceConfig
    public static final Map<String, ServiceConfig<?>> SERVICES_LIST = new ConcurrentHashMap<>(16);

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    //定义全局对外挂起的CompletableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    //维护一个zookeeper实例
    //private ZooKeeper zooKeeper;

    //没有Slf4j注解时需要用这一句
    //private static final Logger logger = LoggerFactory.getLogger(RueRPCBootstrap.class);

    private RueRPCBootstrap() {}


    public static RueRPCBootstrap getInstance() {
        return rueRPCBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     * @param appName
     * @return this
     */
    public RueRPCBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public RueRPCBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return
     */
    public RueRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前服务使用了{}协议进行序列化", protocolConfig);
        }
        return this;
    }



    /**
     * ---------------------------⬇️服务提供方的相关api---------------------------------
     */

    /**
     * 发布服务：将接口->实现注册到服务中心
     * @param service 封装的需要发布的服务
     * @return
     */
    public RueRPCBootstrap publish(ServiceConfig<?> service) {
        //抽象了注册中心，使用注册中心的一个实例完成注册
        registry.register(service);
        SERVICES_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     * @param service
     * @return
     */
    public RueRPCBootstrap publish(List<ServiceConfig<?>> service) {
        for (ServiceConfig<?> serviceConfig : service) {
            this.publish(serviceConfig);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap =  serverBootstrap.group(boss, worker)
                    //工厂方法设计模式实例化一个channel
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //需要添加很多出站和入站的handler
                            socketChannel.pipeline().addLast(new LoggingHandler())
                                    .addLast(new RueRPCRequestDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new RueRPCResponseEncoder());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * ---------------------------⬇️服务调用方的相关api---------------------------------
     */

    public RueRPCBootstrap reference(ReferenceConfig<?> reference) {
        reference.setRegistry(registry);
        return this;
    }
}
