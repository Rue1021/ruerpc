package com.ruerpc.proxy.handler;

import ch.qos.logback.core.subst.Token;
import com.ruerpc.NettyBootstrapInitializer;
import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.annotation.TryTimes;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.discovery.Registry;
import com.ruerpc.enumeration.RequestType;
import com.ruerpc.exceptions.DiscoveryException;
import com.ruerpc.exceptions.NetworkException;
import com.ruerpc.protection.CircuitBreaker;
import com.ruerpc.protection.TokenBucketRateLimiter;
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
import java.net.SocketAddress;
import java.util.*;
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

    /**
     * consumer端所有的方法调用，本质上都会走到这个方法里
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //从方法上拿到TryTimes注解，判断是否需要重试
        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);

        //异常重试次数, 默认值0代表不重试
        int tryTimes = 0;
        //重试间隔时间
        int intervalTime = 0;
        if (tryTimesAnnotation != null) {
            tryTimes = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }

        while (true) {
            try {

                //--------------------------1. 封装报文------------------------------------------
                //封装报文
                RequestPayload requestPayload = RequestPayload.builder()
                        .interfaceName(interfaceRef.getName())
                        .methodName(method.getName())
                        .parametersType(method.getParameterTypes())
                        .parametersValue(args)
                        .returnType(method.getReturnType())
                        .build();
                //创建请求
                RueRPCRequest rueRPCRequest = RueRPCRequest.builder()
                        .requestId(RueRPCBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                        .compressType(CompressorFactory.getCompressor(RueRPCBootstrap.getInstance()
                                .getConfiguration().getCompressType()).getCode())
                        .requestType(RequestType.REQUEST.getId())
                        .serializeType(SerializerFactory.getSerializer(RueRPCBootstrap.getInstance()
                                .getConfiguration().getSerializeType()).getCode())
                        .timeStamp(System.currentTimeMillis())
                        .requestPayload(requestPayload)
                        .build();
                //将请求放入本地线程
                RueRPCBootstrap.REQUEST_THREAD_LOCAL.set(rueRPCRequest);

                //----------2. 发现服务，从注册中心拉取服务列表，并通过客户端负载均衡寻找一个可用服务-----------

                //1. 传入服务的名字，返回ip+端口
                InetSocketAddress address = RueRPCBootstrap.getInstance().getConfiguration()
                        .getLoadBalancer().selectServiceAddress(interfaceRef.getName());
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方，发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
                }

                //获取当前地址所对应的熔断器
                //如果熔断器是打开的，说明当前请求不应该被发送
                Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = RueRPCBootstrap.getInstance()
                        .getConfiguration().getEveryIpCircuitBreaker();
                CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(address);
                if (circuitBreaker == null) {
                    circuitBreaker = new CircuitBreaker(10, 0.5F);
                    everyIpCircuitBreaker.put(address, circuitBreaker);
                }
                //放行心跳检测
                if (rueRPCRequest.getRequestType() != RequestType.HEART_BEAT.getId()
                        && circuitBreaker.isBreak()) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            RueRPCBootstrap
                                    .getInstance()
                                    .getConfiguration()
                                    .getEveryIpCircuitBreaker()
                                    .get(address)
                                    .reset();
                        }
                    }, 3000);
                    throw new RuntimeException("--------> circuit breaker is open, unable to launch request");
                }

                //2. 尝试获取一个可用channel (避免每次调用都会产生一个新的netty连接 -> 缓存channel)
                Channel channel = getAvailableChannel(address);
                if (log.isDebugEnabled()) {
                    log.debug("获取了和【{}】建立的连接通道，准备发送数据", address);
                }

        //---------------------------------------异步策略------------------------------------------

                //4. 写出报文
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();

                //4.1 将completableFuture暴露出去
                RueRPCBootstrap.PENDING_REQUEST.put(rueRPCRequest.getRequestId(), completableFuture);

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

                //写出以后，清理ThreadLocal
                RueRPCBootstrap.REQUEST_THREAD_LOCAL.remove();

                //5. 获得响应的结果
                Object result = completableFuture.get(30, TimeUnit.SECONDS);

                //记录成功请求次数
                circuitBreaker.recordRequest();
                return result;
            } catch (Exception e) {
                tryTimes--;
                //
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ignored) {

                }
                log.error("方法调用失败，进行第【{}】次异常重试", 3 - tryTimes);
                if (tryTimes < 0) {
                    log.error("远程调用过程中，进行了【{}】次重试，方法【{}】依旧不可调用", 3 - tryTimes, method.getName());
                    break;
                }

            }
        }
        throw new RuntimeException("执行对远程方法【{" + method.getName() + "}】的调用失败");
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
