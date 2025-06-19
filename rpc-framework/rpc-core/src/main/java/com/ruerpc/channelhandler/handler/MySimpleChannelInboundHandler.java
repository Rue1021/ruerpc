package com.ruerpc.channelhandler.handler;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.enumeration.ResponseCode;
import com.ruerpc.exceptions.ResponseException;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.protection.CircuitBreaker;
import com.ruerpc.transport.message.RueRPCRequest;
import com.ruerpc.transport.message.RueRPCResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Rue
 * @date 2025/5/28 14:14
 * <p>
 * 测试类，入站handler
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RueRPCResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RueRPCResponse rueRPCResponse) throws Exception {
        //从全局挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = RueRPCBootstrap.PENDING_REQUEST
                .get(rueRPCResponse.getRequestId());

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = RueRPCBootstrap.getInstance()
                .getConfiguration().getEveryIpCircuitBreaker();
        //因为方法调用时已经添加了缓存，所以这里一定能拿到熔断器
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);

        byte responseCode = rueRPCResponse.getResponseCode();
        if (responseCode == ResponseCode.FAILED.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("-------->当前id为[{}]的请求，返回错误的结果，响应码[{}]", rueRPCResponse.getRequestId(), responseCode);
            throw new ResponseException(responseCode, ResponseCode.FAILED.getDescription());
        } else if (responseCode == ResponseCode.RATE_LIMIT.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("-------->当前id为[{}]的请求被限流，响应码[{}]", rueRPCResponse.getRequestId(), responseCode);
            throw new ResponseException(responseCode, ResponseCode.RATE_LIMIT.getDescription());
        } else if (responseCode == ResponseCode.RESOURCE_NOT_FOUND.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("-------->当前id为[{}]的请求，找不到资源，响应码[{}]", rueRPCResponse.getRequestId(), responseCode);
            throw new ResponseException(responseCode, ResponseCode.RESOURCE_NOT_FOUND.getDescription());
        } else if (responseCode == ResponseCode.SUCCESS_HEARTBEAT.getCode()) {
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("------>已找到编号为【{}】的completableFuture，处理心跳检测", rueRPCResponse.getRequestId());
            }
        } else if (responseCode == ResponseCode.CLOSING.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("-------->当前id为[{}]的请求,访问被拒绝，目标服务器正在关闭，响应码[{}]",
                        rueRPCResponse.getRequestId(), responseCode);
            }

            //修正负载均衡器，重新进行负载均衡
            //将正在关闭的服务器从健康列表移除
            RueRPCBootstrap.CHANNEL_CACHE.remove(socketAddress);
            LoadBalancer loadBalancer = RueRPCBootstrap.getInstance().getConfiguration().getLoadBalancer();
            RueRPCRequest rueRPCRequest = RueRPCBootstrap.REQUEST_THREAD_LOCAL.get();

            loadBalancer.reLoadBalance(rueRPCRequest.getRequestPayload().getInterfaceName(),
                    RueRPCBootstrap.CHANNEL_CACHE.keySet().stream().toList());

            throw new ResponseException(responseCode, ResponseCode.CLOSING.getDescription());
        } else {
            //服务提供方给予的结果
            Object returnValue = rueRPCResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("-------->已找到编号为【{}】的completableFuture，处理响应结果", rueRPCResponse.getRequestId());
            }
        }
    }
}

