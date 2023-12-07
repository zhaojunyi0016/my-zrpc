package com.my.rpc;

import com.my.rpc.discovery.Registry;
import com.my.rpc.proxy.handle.RpcConsumerInvocationHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

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
        Class<?>[] classes = new Class[]{interfaceRef};
        InvocationHandler invocationHandler = new RpcConsumerInvocationHandler(registry, interfaceRef);

        // 使用 动态代理, 生成代理对象
        Object proxyObject = Proxy.newProxyInstance(classLoader, classes, invocationHandler);
        return (T) proxyObject;
    }
}
