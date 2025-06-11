package com.ruerpc.proxy.handler;

import com.ruerpc.NettyBootstrapInitializer;
import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.discovery.Registry;
import com.ruerpc.enumeration.RequestType;
import com.ruerpc.exceptions.DiscoveryException;
import com.ruerpc.exceptions.NetworkException;
import com.ruerpc.serialize.SerializerFactory;
import com.ruerpc.transport.message.RequestPayload;
import com.ruerpc.transport.message.RueRPCRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Rue
 * @date 2025/5/28 13:19
 * 该类封装客户端通信的基础逻辑，每个代理对象的远程调用过程都封装在invoke方法中
 * 1. 发现可用服务 2. 建立连接 3. 发起请求，得到结果
 */
@Slf4j
public class RPCConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;
    private final Class<?> interfaceRef;

    public RPCConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //1. 从注册中心发现可用服务 -> 传入服务的名字，返回ip+端口
        InetSocketAddress address = RueRPCBootstrap.LOAD_BALANCER.selectServiceAddress(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("服务调用方，发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
        }

        //2. 尝试获取一个可用channel (避免每次调用都会产生一个新的netty连接 -> 缓存channel)
        Channel channel = getAvailableChannel(address);
        if (log.isDebugEnabled()) {
            log.debug("获取了和【{}】建立的连接通道，准备发送数据", address);
        }

        /*
        ---------------------------------------封装报文------------------------------------------
         */

        //3. 封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();

        RueRPCRequest rueRPCRequest = RueRPCRequest.builder()
                .requestId(RueRPCBootstrap.ID_GENERATOR.getId())
                .requestType(RequestType.REQUEST.getId())
                .compressType(CompressorFactory.getCompressor(RueRPCBootstrap.COMPRESS_TYPE).getCode())
                .serializeType(SerializerFactory.getSerializer(RueRPCBootstrap.SERIALIZE_TYPE).getCode())
                .requestPayload(requestPayload)
                .build();

        /*
        ---------------------------------------异步策略------------------------------------------
         */

        //4. 写出报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();

        //4.1 将completableFuture暴露出去
        RueRPCBootstrap.PENDING_REQUEST.put(1L, completableFuture);

        //4.2 writeAndFlush写出一个请求，这个请求的实例会进入pipeline进行一系列出站操作
        channel.writeAndFlush(rueRPCRequest).addListener(
                (ChannelFutureListener) promise -> {
                    /* 一旦数据被写出去，这个promise就结束了，
                       所以我们需要挂起completableFuture并暴露，并且在得到服务提供方响应的时候调用complete方法
                       pipeline里会调用complete方法，
                       我们要pipeline中最终的handler的处理结果
                     */
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                }
        );

        //5. 获得响应的结果
        return completableFuture.get(30, TimeUnit.SECONDS);
    }


    /**
     * 根据地址获取可用通道
     * @param address
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {

        //1. 尝试从缓存中获取channel
        Channel channel = RueRPCBootstrap.CHANNEL_CACHE.get(address);

        //2. 拿不到就去建立连接
        if (channel == null) {
            //使用addListener实现的异步操作
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise -> {
                        if (promise.isDone()) {
                            if (log.isDebugEnabled()) {
                                log.debug("已经和【{}】成功建立了连接", address);
                            }
                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    }
            );

            //阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发生异常", e);
                throw new DiscoveryException(e);
            }

            RueRPCBootstrap.CHANNEL_CACHE.put(address, channel);
        }

        if (channel == null) {
            log.debug("获取或建立与【{}】的通道时发生异常");
            throw new NetworkException("获取通道时发生异常");
        }
        return channel;
    }
}
