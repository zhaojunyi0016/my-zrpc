package com.my.rpc.channelHandler.handler;

import com.my.rpc.RpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 测试消费端的 处理消息的入站handler
 *
 * @Author : Williams
 * Date : 2023/12/7 17:28
 */
@Slf4j
public class DemoSimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        String result = msg.toString(StandardCharsets.UTF_8);
        log.debug("收到服务端发送的消息 {}", result);
        // 从全局的挂起的请求中, 寻找阈值匹配的待处理的 completableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
