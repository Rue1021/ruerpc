package com.ruerpc.core;

import com.ruerpc.NettyBootstrapInitializer;
import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.discovery.Registry;
import com.ruerpc.enumeration.RequestType;
import com.ruerpc.serialize.SerializerFactory;
import com.ruerpc.transport.message.RueRPCRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Rue
 * @date 2025/6/11 18:48
 */
@Slf4j
public class HeartbeatDetector {

    public static void detectHeartbeat(String serviceName) {
        //拉取服务
        Registry registry = RueRPCBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(serviceName);

        //建立连接
        for (InetSocketAddress address : addresses) {
            Channel channel = null;
            if (RueRPCBootstrap.CHANNEL_CACHE.containsKey(address)) {
                channel = RueRPCBootstrap.CHANNEL_CACHE.get(address);
                break;
            }
            try {
                channel = NettyBootstrapInitializer
                        .getBootstrap()  //拿到Netty的初始化器
                        .connect(address)  //建立连接
                        .sync() //拿到ChannelFuture
                        .channel(); //拿到一个连接
                RueRPCBootstrap.CHANNEL_CACHE.put(address, channel);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //定时任务
        Thread thread = new Thread(() -> {
            new Timer().schedule(new MyTimerTask(), 0, 2000);
        }, "ruerpc-heartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();

    }

    private static class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            RueRPCBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();

            Map<InetSocketAddress, Channel> cache = RueRPCBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry: cache.entrySet()) {
                //定义一个重试次数
                int tryTime = 3;
                while(tryTime > 0) {
                    Channel channel = entry.getValue();

                    long start = System.currentTimeMillis();

                    //构建一个心跳请求
                    RueRPCRequest rueRPCRequest4Heartbeat = RueRPCRequest.builder()
                            .requestId(RueRPCBootstrap.ID_GENERATOR.getId())
                            .requestType(RequestType.HEART_BEAT.getId())
                            .compressType(CompressorFactory.getCompressor(RueRPCBootstrap.COMPRESS_TYPE).getCode())
                            .serializeType(SerializerFactory.getSerializer(RueRPCBootstrap.SERIALIZE_TYPE).getCode())
                            .timeStamp(start)
                            .build();

                    //4. 写出报文
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    //4.1 将completableFuture暴露出去
                    RueRPCBootstrap.PENDING_REQUEST.put(rueRPCRequest4Heartbeat.getRequestId(), completableFuture);
                    //4.2 writeAndFlush写出一个请求，这个请求的实例会进入pipeline进行一系列出站操作
                    channel.writeAndFlush(rueRPCRequest4Heartbeat).addListener(
                            (ChannelFutureListener) promise -> {
                                if (!promise.isSuccess()) {
                                    completableFuture.completeExceptionally(promise.cause());
                                }
                            }
                    );

                    Long endTime = 0L;
                    try {
                        //只阻塞等待1 s
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (ExecutionException | InterruptedException | TimeoutException e) {
                        tryTime--;
                        log.error("和地址为【{}】的主机的连接发生异常, 正在进行第【{}】次重试",
                                channel.remoteAddress(),
                                3 - tryTime);
                        //重试次数用完后还未连接上，就将失效的地址移出服务列表
                        if (tryTime == 0) {
                            RueRPCBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        //设置两次重试之间的间隔
                        try {
                            Thread.sleep(10 * (new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    Long time = endTime - start;
                    RueRPCBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.debug("心跳检测，【{}】服务器的响应时间是【{}】", entry.getKey(), time);
                    break;
                }
            }
            log.info("--------    -------本次心跳检测生成的treemap:-------------------");
            for (Map.Entry<Long, Channel> entry: RueRPCBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("-----[key]: {}, [value]: {}-----", entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
