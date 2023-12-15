package com.my.rpc.channelHandler.handler;

import com.my.rpc.RpcBootstrap;
import com.my.rpc.enums.ResponseEnum;
import com.my.rpc.exception.RpcRespException;
import com.my.rpc.protection.breaker.Breaker;
import com.my.rpc.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * 测试消费端的 处理消息的入站handler
 *
 * @Author : Williams
 * Date : 2023/12/7 17:28
 */
@Slf4j
public class CompleteSimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        // 从全局的挂起的请求中, 寻找匹配的待处理的 completableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(response.getRequestId());

        SocketAddress socketAddress = ctx.channel().remoteAddress();
        // 一定不为空 在调用目标方法的时候,代理类中已经初始化过
        Breaker breaker = RpcBootstrap.getInstance().getConfiguration().everyIpBreaker.get(socketAddress);
        final byte code = response.getCode();
        if (code == ResponseEnum.FAIL.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.debug("requestId {}, 返回错误的结果, 响应码 ={}", response.getRequestId(), response.getCode());
            throw new RpcRespException(ResponseEnum.FAIL.getDesc(), code);
        } else if (code == ResponseEnum.RATE_LIMIT.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.debug("requestId {}, 被限流, 响应码 ={}", response.getRequestId(), response.getCode());
            throw new RpcRespException(ResponseEnum.RATE_LIMIT.getDesc(), code);
        } else if (code == ResponseEnum.RESOURCE_NOT_FOUND.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.debug("requestId {}, 找不到资源, 响应码 ={}", response.getRequestId(), response.getCode());
            throw new RpcRespException(ResponseEnum.RESOURCE_NOT_FOUND.getDesc(), code);
        } else if (code == ResponseEnum.SUCCESS.getCode() || code == ResponseEnum.HEARTBEAT.getCode()) {
            final Object returnValue = response.getBody() == null ? 1 : response.getBody();
            log.debug("收到服务端的响应结果, 并且对之complete {}", returnValue);
            completableFuture.complete(returnValue);
        }
    }
}
