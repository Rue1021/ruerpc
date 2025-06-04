package com.ruerpc.channelHandler;

import com.ruerpc.channelHandler.handler.MySimpleChannelInboundHandler;
import com.ruerpc.channelHandler.handler.RueRPCRequestEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


/**
 * @author Rue
 * @date 2025/5/28 14:17
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        socketChannel.pipeline()
                //netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                //消息编码器
                .addLast(new RueRPCRequestEncoder())
                /*
                ------⬆️出站------
                ------⬇️入站------
                 */
                .addLast(new MySimpleChannelInboundHandler());
    }
}
