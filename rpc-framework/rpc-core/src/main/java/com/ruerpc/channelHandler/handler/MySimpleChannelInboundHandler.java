package com.ruerpc.channelHandler.handler;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.transport.message.RueRPCResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @author Rue
 * @date 2025/5/28 14:14
 * 测试类，入站handler
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RueRPCResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RueRPCResponse rueRPCResponse) throws Exception {
        //服务提供方给予的结果
        Object returnValue = rueRPCResponse.getBody();

        //从全局挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = RueRPCBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
    }
}
