package com.my.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author : Williams
 * Date : 2023/12/5 15:58
 */
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

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
                System.out.println("拿到代理类");
                return null;
            }
        });
        return (T) object;
    }
}
