package com.my.rpc.channelHandler;

import com.my.rpc.channelHandler.handler.DemoSimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author : Williams
 * Date : 2023/12/7 17:31
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new DemoSimpleChannelInboundHandler());
    }
}
