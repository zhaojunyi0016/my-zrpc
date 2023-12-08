package com.my.rpc.channelHandler;

import com.my.rpc.channelHandler.handler.DemoSimpleChannelInboundHandler;
import com.my.rpc.channelHandler.handler.RpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 服务调用方的初始化 handler
 *
 * @Author : Williams
 * Date : 2023/12/7 17:31
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // netty 默认自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 消息编码器
                .addLast(new RpcMessageEncoder())
                // TODO
                .addLast(new DemoSimpleChannelInboundHandler());
    }
}
