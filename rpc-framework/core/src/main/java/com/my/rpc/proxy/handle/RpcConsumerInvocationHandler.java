package com.my.rpc.proxy.handle;

import com.my.rpc.ConsumerNettyBootstrapInitializer;
import com.my.rpc.RpcBootstrap;
import com.my.rpc.annotation.TryTimes;
import com.my.rpc.discovery.Registry;
import com.my.rpc.enums.CompressEnum;
import com.my.rpc.enums.RequestEnum;
import com.my.rpc.enums.SerializeEnum;
import com.my.rpc.exception.DiscoveryException;
import com.my.rpc.exception.NetException;
import com.my.rpc.exception.RpcCallException;
import com.my.rpc.transport.message.RequestPayload;
import com.my.rpc.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
     * 所有的方法调用本质都会走到这里
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return 目标方法返回值
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 获取当前配置的负载均衡器
        InetSocketAddress address = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectAddress(interfaceRef.getName());

        // 获取一个 channel
        Channel channel = getAvailableChannel(address);

        // 封装报文对象
        long requestId = RpcBootstrap.getInstance().getConfiguration().getSnowflakeIdGenerator().getId();
        RpcRequest rpcRequest = getRpcRequest(method, args, requestId);

        // 服务端返回的结果
        CompletableFuture<Object> resultFuture = new CompletableFuture<>();
        RpcBootstrap.PENDING_REQUEST.put(requestId, resultFuture);
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

        // 从方法中获取是否带了重试注解, 没有的话只执行一次
        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);
        int tryTimes = 0;
        long intervalTime = 0;
        if (tryTimesAnnotation != null) {
            tryTimes = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }
        while (true) {
            try {
                Object result = resultFuture.get(1, TimeUnit.SECONDS);
                log.debug("请求Id={}, 发起调用获取最终结果 = [{}]", rpcRequest.getRequestId(), result);
                return result;
            } catch (Exception e) {
                // 重试次数 -1
                tryTimes--;
                try {
                    // 每次递增, 如果时间一样 可能发生风暴问题
                    Thread.sleep(intervalTime * (3 - tryTimes));
                } catch (InterruptedException ex) {
                    log.error("An exception occurred during the call retry interval... error ={}", e);
                }
                if (tryTimes <= 0) {
                    log.error("对方法[{}] 进行远程调用时, 重试{}次, 已超过最大次数", method.getName(), 3 - tryTimes);
                    break;
                }
            }
        }
        throw new RpcCallException("Call exception methed = " + method.getName());
    }


    /**
     * 获取一个可用的 channel
     *
     * @param address ip 地址
     * @return io.netty.channel.Channel
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


    /**
     * 构建报文对象
     *
     * @param method    方法
     * @param args      参数
     * @param requestId 请求 id
     * @return RpcRequest
     */
    private RpcRequest getRpcRequest(Method method, Object[] args, long requestId) {
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType()).build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(requestId)
                .requestType(RequestEnum.REQUEST.getId())
                .compressType(CompressEnum.getCodeByDesc(RpcBootstrap.getInstance().getConfiguration().getCompressMode()))
                .serializeType(SerializeEnum.getCodeByDesc(RpcBootstrap.getInstance().getConfiguration().getSerializeMode()))
                .timestamp(new Date().getTime())
                .requestPayload(requestPayload).build();
        return rpcRequest;
    }
}
