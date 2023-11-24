package com.my.rpc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 客户端
 *
 * @Author : Williams
 * Date : 2023/11/23 18:24
 */
public class AppServer {

    private int port;

    public AppServer(int port) {
        this.port = port;
    }

    public void start() {
        // 1. 创建 bossGroup, 只负责处理请求 IO , 之后会将请求分发到 workGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workGroup = new NioEventLoopGroup(10);
        try {

            // 需要服务器引导程序 ServerBootStrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 配置服务器
            serverBootstrap = serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ServerChannelHandler());
                        }
                    });
            // 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            // 接受客户端发送的消息
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bossGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                workGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AppServer appServer = new AppServer(8080);
        appServer.start();
    }
}
