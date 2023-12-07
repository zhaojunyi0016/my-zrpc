package com.my.rpc;

import com.my.rpc.discovery.Registry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @Author : Williams
 * Date : 2023/12/5 15:58
 */
@Slf4j
@Data
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    private Registry registry;


    /**
     * 获取对应类的代理类
     *
     * @return
     */
    public T get() {
        // 使用动态代理完成对应工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] interfaces = new Class[]{interfaceRef};

        // 使用 动态代理, 生成代理对象
        Object object = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.debug("method =={}", method);
                log.debug("args =={}", args);
                // 1. 发现服务, 从注册中心, 寻找一个可用的服务
                // 传入服务的名字, 返回 ip+端口

                // TODO 每次都需要重新拉取服务列表吗, 本地缓存+watch 机制
                // TODO 如何合理的选择一个可用的服务, 而不是 get(0)  负载均衡
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                // 2. 使用 netty 连接服务器, 调用方法,得到返回结果
                log.info("addresss={}", address);
                return null;
            }
        });
        return (T) object;
    }
}
