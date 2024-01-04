package com.my.rpc;

import com.my.rpc.annotation.RpcReference;
import com.my.rpc.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @Author : Williams
 * Date : 2023/12/16 16:14
 */
@Component
@Slf4j
public class RpcProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 生成一个代理对象
                Class<?> type = field.getType();
                Object proxy = ProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    log.error("获取 RpcReference 注解上类的代理对象失败.. error {} ", e);
                    throw new RuntimeException(e);
                }
            }
        }
         return bean;
    }
}
