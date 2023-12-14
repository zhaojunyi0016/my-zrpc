package com.my.rpc;

import com.my.rpc.channelHandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取一个 bootstrap 单例
 *
 * @Author : Williams
 * Date : 2023/12/7 14:52
 */
@Slf4j
public class ConsumerNettyBootstrapInitializer {

    // 启动 netty 客户端引导类
    private static Bootstrap bootstrap = new Bootstrap();


    // 初始化
    static {
        // 定义线程池 eventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap = bootstrap.group(group)
                // 选择怎么样的的 channel
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    private ConsumerNettyBootstrapInitializer() {
    }

    public static Bootstrap getBootstrap() {
        // 启动一个客户端需要一个辅助类  bootStrap
        return bootstrap;
    }
}
