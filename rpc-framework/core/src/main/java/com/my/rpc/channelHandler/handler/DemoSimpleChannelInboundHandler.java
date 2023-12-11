package com.my.rpc.channelHandler.handler;

import com.my.rpc.RpcBootstrap;
import com.my.rpc.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 测试消费端的 处理消息的入站handler
 *
 * @Author : Williams
 * Date : 2023/12/7 17:28
 */
@Slf4j
public class DemoSimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        Object returnValue = response.getBody();
        log.debug("收到服务端的响应结果, 并且对之complete {}", returnValue);
        // 从全局的挂起的请求中, 寻找匹配的待处理的 completableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(response.getRequestId());
        completableFuture.complete(returnValue);
    }
}
