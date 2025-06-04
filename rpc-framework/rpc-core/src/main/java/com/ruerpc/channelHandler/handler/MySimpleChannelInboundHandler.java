package com.ruerpc.channelHandler.handler;

import com.ruerpc.RueRPCBootstrap;
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
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        //服务提供方给予的结果
        String result = msg.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = RueRPCBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
