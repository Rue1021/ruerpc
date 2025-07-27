package com.ruerpc.channelhandler.handler;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.enumeration.ResponseCode;
import com.ruerpc.enumeration.ResponseCodeConstant;
import com.ruerpc.exceptions.ResponseException;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.protection.CircuitBreaker;
import com.ruerpc.transport.message.RueRPCRequest;
import com.ruerpc.transport.message.RueRPCResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.ruerpc.enumeration.ResponseCodeConstant.*;

/**
 * @author Rue
 * @date 2025/5/28 14:14
 * <p>
 * 客户端的一个入站handler
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
        CircuitBreaker circuitBreaker = RueRPCBootstrap
                .getInstance()
                .getConfiguration()
                .getEveryIpCircuitBreaker()
                //因为方法调用时已经添加了缓存，所以这里一定能拿到熔断器
                .get(socketAddress);

        byte responseCode = rueRPCResponse.getResponseCode();

        switch (responseCode) {
            case SUCCESS:
                completableFuture.complete(rueRPCResponse.getBody());
                log.debug("请求[{}]成功，准备处理响应结果", rueRPCResponse.getRequestId());
                break;

            case SUCCESS_HEARTBEAT:
                completableFuture.complete(null);
                log.debug("心跳检测成功");
                break;

            case RATE_LIMIT:
            case RESOURCE_NOT_FOUND:
            case FAILED:
            case CLOSING:
                handleErrorResponse(rueRPCResponse, completableFuture, circuitBreaker,socketAddress);
                break;

            default:
                completableFuture.completeExceptionally(
                        new ResponseException(rueRPCResponse.getResponseCode(), "未知响应码")
                );
        }
    }

    /**
     * 处理成功和心跳检测以外的其他异常
     *
     * @param rueRPCResponse 响应
     * @param completableFuture 待处理的completableFuture
     * @param circuitBreaker 熔断器
     * @param address 服务器地址
     */
    private void handleErrorResponse(RueRPCResponse rueRPCResponse,
                                     CompletableFuture<Object> completableFuture,
                                     CircuitBreaker circuitBreaker,
                                     SocketAddress address) {
        byte responseCode = rueRPCResponse.getResponseCode();
        circuitBreaker.recordErrorRequest();

        if (responseCode == ResponseCodeConstant.CLOSING) {
            //将正在关闭的服务器从健康列表移除
            RueRPCBootstrap.CHANNEL_CACHE.remove(address);

            //拿到负载均衡器和接口方法，重新负载均衡
            LoadBalancer loadBalancer = RueRPCBootstrap.getInstance().getConfiguration().getLoadBalancer();
            RueRPCRequest rueRPCRequest = RueRPCBootstrap.REQUEST_THREAD_LOCAL.get();
            loadBalancer.reLoadBalance(rueRPCRequest.getRequestPayload().getInterfaceName(),
                    RueRPCBootstrap.CHANNEL_CACHE.keySet().stream().toList());
        }

        completableFuture.completeExceptionally(
                new ResponseException(responseCode, ResponseCode.CLOSING.getDescription())
        );
        log.error("请求[{}]失败，代码: {}", rueRPCResponse.getRequestId(), responseCode);
    }

}

