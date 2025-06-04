package com.ruerpc.channelHandler.handler;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.ServiceConfig;
import com.ruerpc.transport.message.RequestPayload;
import com.ruerpc.transport.message.RueRPCRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Rue
 * @date 2025/6/4 15:09
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RueRPCRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RueRPCRequest rueRPCRequest) throws Exception {
        //1. 获取负载
        RequestPayload requestPayload = rueRPCRequest.getRequestPayload();
        //2. 根据负载内容进行方法调用
        Object object = callTargetMethod(requestPayload);
        //3. 封装响应
        //4. 写出响应
        channelHandlerContext.channel().writeAndFlush(null);
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
            log.error("调用服务【{}】的方法【{}】时发生异常",interfaceName, methodName, e);
            throw new RuntimeException(e);
        }

        return returnValue;
    }
}
