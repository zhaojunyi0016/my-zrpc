package com.my.rpc.channelHandler.handler;

import com.my.rpc.RpcBootstrap;
import com.my.rpc.ServiceConfig;
import com.my.rpc.transport.message.RequestPayload;
import com.my.rpc.transport.message.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Author : Williams
 * Date : 2023/12/8 16:33
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        // 1. 获取负载内容
        RequestPayload requestPayload = rpcRequest.getRequestPayload();
        // 2. 根据负载内容进行方法调用
        Object object = callTargetMethod(requestPayload, rpcRequest.getRequestId());

        // 3. 封装响应 TODO

        // 4. 返回给调用方
        ctx.channel().writeAndFlush(object);
    }


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
            throw new RuntimeException(e);
        }
        return result;
    }
}
