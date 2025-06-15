package com.ruerpc;

import com.ruerpc.channelhandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rue
 * @date 2025/5/23 15:01
 *
 * 提供bootstrap单例
 */
@Slf4j
public class NettyBootstrapInitializer {

    private static Bootstrap bootstrap = new Bootstrap();

    static {
        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                //channel初始化
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {
    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
