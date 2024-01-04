package com.my.rpc.proxy;

import com.my.rpc.ReferenceConfig;
import com.my.rpc.RpcBootstrap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : Williams
 * Date : 2023/12/16 17:56
 */
public class ProxyFactory {

    public static Map<Class<?>, Object> classCache = new ConcurrentHashMap<>();

    public static <T> T getProxy(Class<T> clazz) {
        Object bean = classCache.get(clazz);
        if (bean != null) {
            return (T) bean;
        }
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(clazz);

        RpcBootstrap.getInstance()
                .registry()
                .reference(reference);
        T t = reference.get();
        classCache.put(clazz, t);
        return t;
    }
}
