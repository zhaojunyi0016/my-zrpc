package com.my.rpc.proxy.handle;

import com.my.rpc.ConsumerNettyBootstrapInitializer;
import com.my.rpc.RpcBootstrap;
import com.my.rpc.discovery.Registry;
import com.my.rpc.enums.RequestEnum;
import com.my.rpc.exception.DiscoveryException;
import com.my.rpc.exception.NetException;
import com.my.rpc.transport.message.RequestPayload;
import com.my.rpc.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 封装了客户端通信的基础逻辑, 每一个代理对象的远程调用过程都封在 invoke()
 * 1. 发现可用服务
 * 2. 建立连接
 * 3. 发送请求
 * 4. 得到响应
 *
 * @Author : Williams
 * Date : 2023/11/23 18:34
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    /**
     * 注册中心
     */
    final private Registry registry;

    final private Class<?> interfaceRef;


    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }


    /**
     * 调用代理类的方法
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 发现服务, 从注册中心, 寻找一个可用的服务
        InetSocketAddress address = registry.lookup(interfaceRef.getName());

        // 获取一个 channel
        Channel channel = getAvailableChannel(address);

        // 封装报文对象
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType()).build();
        // TODO 对 id 和类型做动态处理
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(1L)
                .requestType(RequestEnum.REQUEST.getId())
                .compressType((byte) 1)
                .serializeType((byte) 1)
                .requestPayload(requestPayload).build();

        // 服务端返回的结果
        CompletableFuture<Object> resultFuture = new CompletableFuture<>();
        RpcBootstrap.PENDING_REQUEST.put(1L, resultFuture);
        // 发送请求
        channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
            // 发送出去经过 pipeline 加工处理
            // 捕获 发数据的结果是否异常
            if (!promise.isSuccess()) {
                resultFuture.completeExceptionally(promise.cause());
            }
        });

        // 如果没有人处理 resultFuture, 这里会阻塞,  等到 complete 方法的执行
        // Q: 我们需要在哪里调用 complete(),  A: pipeline 中最终的 handle 处理的结果
        return resultFuture.get(5, TimeUnit.SECONDS);
    }


    /**
     * 获取一个可用的 channel
     *
     * @param address ip 地址
     * @return Channel
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        // 从缓存中获取 channel
        Channel channel = RpcBootstrap.CHANNEL_CACHE.get(address);
        // 如果未获取到, 则创建新的 channel
        if (channel == null) {
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            ConsumerNettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                // 异步处理
                if (promise.isDone()) {
                    channelFuture.complete(promise.channel());
                } else if (!promise.isSuccess()) {
                    channelFuture.completeExceptionally(promise.cause());
                }
            });

            try {
                // 这里阻塞了, 相当于还是同步的, 但是可以拓展, 可以在其他位置获取, 暂时放在这
                channel = channelFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("获取 channel 时候异常, error = {}", e);
                throw new DiscoveryException(e);
            }
            RpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            throw new NetException("获取 channel 异常");
        }
        return channel;
    }
}
