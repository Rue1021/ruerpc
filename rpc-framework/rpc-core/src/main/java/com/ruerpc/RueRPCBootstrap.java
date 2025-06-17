package com.ruerpc;

import com.ruerpc.annotation.RueRPCApi;
import com.ruerpc.channelhandler.handler.MethodCallHandler;
import com.ruerpc.channelhandler.handler.RueRPCRequestDecoder;
import com.ruerpc.channelhandler.handler.RueRPCResponseEncoder;
import com.ruerpc.config.Configuration;
import com.ruerpc.core.HeartbeatDetector;
import com.ruerpc.discovery.RegistryConfig;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.transport.message.RueRPCRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Rue
 * @date 2025/5/20 12:28
 *
 * 启动引导类
 */
@Slf4j
public class RueRPCBootstrap {

    //RueRPCBootstrap是个单例，即希望每个应用程序只有一个实例，用饿汉式写
    private static final RueRPCBootstrap rueRPCBootstrap = new RueRPCBootstrap();

    //全局的配置中心
    private Configuration configuration;

    //用当前线程保存request常量
    public static final ThreadLocal<RueRPCRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    //维护已经发布并暴露的服务列表 key -> interface的全限定名 value -> ServiceConfig
    public static final Map<String, ServiceConfig<?>> SERVICES_LIST = new ConcurrentHashMap<>(16);

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    //treemap有序，用来实现心跳检测缓存以及最短响应时间负载均衡策略
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    //定义全局对外挂起的CompletableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);


    private RueRPCBootstrap() {
        //构造一个上下文
        configuration = new Configuration();
    }

    public static RueRPCBootstrap getInstance() {
        return rueRPCBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     * @param appName
     * @return this
     */
    public RueRPCBootstrap application(String appName) {
        configuration.setAppName(appName);
        return this;
    }

    /**
     * 配置一个注册中心
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public RueRPCBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }

    /**
     * 配置负载均衡策略
     * @param loadBalancer 负载均衡器
     * @return this当前实例
     */
    public RueRPCBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
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
        configuration.getRegistryConfig().getRegistry().register(service);
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
            //绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();

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

    /**
     * 好像是用来开启服务的
     * @param reference
     * @return
     */
    public RueRPCBootstrap reference(ReferenceConfig<?> reference) {
        //开启对这个服务的心跳检测
        HeartbeatDetector.detectHeartbeat(reference.getInterfaceRef().getName());
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;
    }

    /**
     * 配置序列化的方式
     * @param serializeType
     * @return
     */
    public RueRPCBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()) {
            log.debug("配置了序列化方式【{}】", serializeType);
        }
        return this;
    }

    /**
     * 配置使用的压缩方式
     * @param compressType
     * @return
     */
    public RueRPCBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()) {
            log.debug("配置了压缩算法【{}】", compressType);
        }
        return this;
    }

    /**
     * 扫描包，进行批量注册
     * @param packageName 包名
     * @return this本身
     */
    public RueRPCBootstrap scan(String packageName) {
        //扫描指定包下所有.class文件并提取类的全限定名
        List<String> classNames = getAllClassName(packageName);
        //通过反射获取它的接口，构建具体实现
        List<? extends Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(clazz -> clazz.getAnnotation(RueRPCApi.class) != null)
                .toList();
        //针对每一个扫描到的class都进行一次发布
        for (Class<?> clazz : classes) {
            //获取接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                //setRef就是实例
                serviceConfig.setRef(instance);
                //发布服务
                publish(serviceConfig);
                if (log.isDebugEnabled()) {
                    log.debug("------>> 已经通过包扫描，将服务【{}】发布", anInterface);
                }
            }
        }

        return this;
    }

    private List<String> getAllClassName(String packageName) {
        String basePath = packageName.replace(".", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时，发现路径不存在");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        //classNames = recursionFile(absolutePath, classNames, basePath);
        Path start = Paths.get(absolutePath);
        try {
            Files.walk(start)
                    .filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> {
                        String className = getClassNameByAbsolutePath(p.toString(), basePath);
                        classNames.add(className);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classNames;
    }



//    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
//        //获取文件
//        File file = new File(absolutePath);
//
//        if (file.isDirectory()) {
//            File[] childFiles = file.listFiles(pathname -> pathname.isDirectory() ||
//                    pathname.getPath().contains(".class"));
//            if (childFiles == null || childFiles.length == 0) {
//                return classNames;
//            }
//            for (File childFile : childFiles) {
//                if (childFile.isDirectory()) {
//                    //递归调用
//                    recursionFile(childFile.getAbsolutePath(), classNames, basePath);
//                } else {
//                    String className = getClassNameByAbsolutePath(childFile.getAbsolutePath(), basePath);
//                    classNames.add(className);
//                }
//            }
//        } else {
//            String className = getClassNameByAbsolutePath(absolutePath, basePath);
//            classNames.add(className);
//        }
//        return classNames;
//    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath))
                .replace("/", ".");
        fileName = fileName.substring(0, fileName.indexOf(".class"));
        return fileName;
    }


    public Configuration getConfiguration() {
        return configuration;
    }
}
