package com.my.rpc.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 客户端
 *
 * @Author : Williams
 * Date : 2023/11/23 18:12
 */
public class AppClient implements Serializable {

    private static final long serialVersionUID = -3351492880977440034L;

    public void run() {
        // 定义线程池 eventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();

        // 启动一个客户端需要一个辅助类  bootStrap
        Bootstrap bootstrap = new Bootstrap();

        bootstrap = bootstrap.group(group)
                .remoteAddress(new InetSocketAddress(8080))
                 // 选择怎么样的的 channel
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    // 处理器要处理的内容
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ClientChannelHandler());
                    }
                });


        ChannelFuture channelFuture = null;
        try {
            // 连接服务器
            channelFuture = bootstrap.connect().sync();
            // 获取 channel, 并且写出数据
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello".getBytes(StandardCharsets.UTF_8)));

            // 阻塞 , 等待接受数据
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AppClient appClient = new AppClient();
        appClient.run();
    }

}
