package com.ruerpc.channelhandler;

import com.ruerpc.channelhandler.handler.MySimpleChannelInboundHandler;
import com.ruerpc.channelhandler.handler.RueRPCRequestEncoder;
import com.ruerpc.channelhandler.handler.RueRPCResponseDecoder;
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
                //入站解码器
                .addLast(new RueRPCResponseDecoder())
                //处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }
}
