package com.my.rpc.channelHandler.handler;

import com.my.rpc.RpcBootstrap;
import com.my.rpc.ServiceConfig;
import com.my.rpc.enums.RequestEnum;
import com.my.rpc.enums.ResponseEnum;
import com.my.rpc.exception.RpcCallException;
import com.my.rpc.hook.ShutdownHolder;
import com.my.rpc.protection.retelimiter.ReteLimiter;
import com.my.rpc.protection.retelimiter.impl.TokenBuketRateLimiter;
import com.my.rpc.transport.message.RequestPayload;
import com.my.rpc.transport.message.RpcRequest;
import com.my.rpc.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @Author : Williams
 * Date : 2023/12/8 16:33
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = RpcResponse.builder()
                .requestId(rpcRequest.getRequestId())
                .compressType(rpcRequest.getCompressType())
                .serializeType(rpcRequest.getSerializeType()).build();

        // 挡板开启直接响应
        if (ShutdownHolder.BAFFLE.get()) {
            response.setCode(ResponseEnum.SERVER_CLOSING.getCode());
            ctx.channel().writeAndFlush(response);
            return;
        }


        // 心跳不限流
        if (rpcRequest.getRequestType() == RequestEnum.HEART_BEAT.getId()) {
            log.debug("服务端..响应心跳请求");
            // 封装响应报文, 心跳没有 body
            response.setCode(ResponseEnum.HEARTBEAT.getCode());
            ctx.channel().writeAndFlush(response);
            return;
        }

        ShutdownHolder.REQUEST_COUNTER.increment();

        // 限流器
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        ReteLimiter ipRateLimiter = RpcBootstrap.getInstance().getConfiguration().everyIpRateLimiter.get(socketAddress);
        if (ipRateLimiter == null) {
            ipRateLimiter = new TokenBuketRateLimiter(500, 100);
            RpcBootstrap.getInstance().getConfiguration().everyIpRateLimiter.put(socketAddress, ipRateLimiter);
        }
        if (!ipRateLimiter.allowRequest()) {
            log.warn("服务器限流达到最大限度, 拒绝访问");
            response.setCode(ResponseEnum.RATE_LIMIT.getCode());
        } else if (rpcRequest.getRequestType() == RequestEnum.REQUEST.getId()) {
            // 1. 获取负载内容
            RequestPayload requestPayload = rpcRequest.getRequestPayload();
            // 2. 根据负载内容进行方法调用
            try {
                Object result = callTargetMethod(requestPayload, rpcRequest.getRequestId());
                log.debug("服务端对请求 id ={} 的调用结束, 返回结果为 =[{}] ", rpcRequest.getRequestId(), result);
                // 封装成功响应报文
                response.setCode(ResponseEnum.SUCCESS.getCode());
                response.setBody(result);
            } catch (RpcCallException e) {
                // 封装失败响应报文
                response.setCode(ResponseEnum.FAIL.getCode());
            }
        }
        // 4. 写出结果, 交给下一个 pipeline
        ctx.channel().writeAndFlush(response);

        ShutdownHolder.REQUEST_COUNTER.decrement();
    }


    /**
     * 通过反射调用方法
     *
     * @param requestPayload 负载对象
     * @param requestId      请求 id
     * @return 方法返回
     */
    private Object callTargetMethod(RequestPayload requestPayload, long requestId) {
        // 接口名
        String interfaceName = requestPayload.getInterfaceName();
        // 方法名
        String methodName = requestPayload.getMethodName();
        // 入参类型
        Class<?>[] parametersType = requestPayload.getParametersType();
        // 入参值
        Object[] parametersValue = requestPayload.getParametersValue();
        // 发布服务的时候将具体接口都缓存起来了
        Map<String, ServiceConfig<?>> serviceList = RpcBootstrap.SERVICE_LIST;
        ServiceConfig<?> serviceConfig = serviceList.get(interfaceName);
        // 获取对应实现类
        Object interfaceImpl = serviceConfig.getRef();

        // 通过反射调用 1. 获取方法对象 2. 执行 invoke 方法
        Object result = null;
        try {
            Class<?> aClass = interfaceImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
            result = method.invoke(interfaceImpl, parametersValue);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("反射调用方法 {} 失败,  requestId ={} error ={}", methodName, requestId, e);
            throw new RpcCallException(e);
        }
        return result;
    }
}
