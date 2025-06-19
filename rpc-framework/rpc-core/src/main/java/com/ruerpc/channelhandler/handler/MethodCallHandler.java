package com.ruerpc.channelhandler.handler;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.ServiceConfig;
import com.ruerpc.enumeration.RequestType;
import com.ruerpc.enumeration.ResponseCode;
import com.ruerpc.protection.RateLimiter;
import com.ruerpc.protection.TokenBucketRateLimiter;
import com.ruerpc.transport.message.MessageFormatConstant;
import com.ruerpc.transport.message.RequestPayload;
import com.ruerpc.transport.message.RueRPCRequest;
import com.ruerpc.transport.message.RueRPCResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;

/**
 * @author Rue
 * @date 2025/6/4 15:09
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RueRPCRequest> {

    /**
     * Netty 框架在收到匹配类型的消息时自动调用的回调方法
     *
     * @param channelHandlerContext
     * @param rueRPCRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RueRPCRequest rueRPCRequest) throws Exception {

        //进来先封装部分响应
        RueRPCResponse rueRPCResponse = RueRPCResponse.builder()
                .requestId(rueRPCRequest.getRequestId())
                .compressType(rueRPCRequest.getCompressType())
                .serializeType(rueRPCRequest.getSerializeType())
                .timeStamp(System.currentTimeMillis())
                .build();

        //限流
        Channel channel = channelHandlerContext.channel();
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = RueRPCBootstrap
                .getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBucketRateLimiter(10, 10);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }

        if (!rateLimiter.allowRequest()) {
            //如果请求被限流器拦截，就需要在此给出返回的逻辑
            rueRPCResponse.setResponseCode(ResponseCode.RATE_LIMIT.getCode());
        } else if (rueRPCRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            //处理心跳请求 -- 封装响应并返回
            rueRPCResponse.setResponseCode(ResponseCode.SUCCESS_HEARTBEAT.getCode());
        } else {
            //--------------------------具体的调用过程-------------------------
            //1. 获取负载
            RequestPayload requestPayload = rueRPCRequest.getRequestPayload();

            try {
                //2. 根据负载内容进行方法调用
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用", rueRPCRequest.getRequestId());
                }
                //3. 完成整个响应的封装
                rueRPCResponse.setBody(result);
                rueRPCResponse.setResponseCode(ResponseCode.SUCCESS.getCode());
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("请求id【{}】在方法调用过程中发生异常", rueRPCRequest.getRequestId(), e);
                }
                rueRPCResponse.setResponseCode(ResponseCode.FAILED.getCode());
            }
        }

        //4. 写出响应
        channel.writeAndFlush(rueRPCResponse);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        //寻找匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = RueRPCBootstrap.SERVICES_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        //通过反射调用
        Class<?> aClass = refImpl.getClass();
        Method method = null;
        try {
            method = aClass.getMethod(methodName, parametersType);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Object returnValue = null;
        try {
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("调用服务【{}】的方法【{}】时发生异常", interfaceName, methodName, e);
            throw new RuntimeException(e);
        }

        return returnValue;
    }
}
