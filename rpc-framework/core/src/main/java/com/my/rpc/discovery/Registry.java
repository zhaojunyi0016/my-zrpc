package com.my.rpc.discovery;

import com.my.rpc.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 注册中心
 *
 * @Author : Williams
 * Date : 2023/12/6 16:40
 */
public interface Registry {

    /**
     * 发布服务
     *
     * @param serviceConfig 服务的配置内容
     */
    void publish(ServiceConfig<?> serviceConfig);


    /**
     * 从注册中心中, 拉取可用的服务
     *
     * @param serviceName 方法的全限定名
     * @return 服务的地址
     */
    InetSocketAddress lookup(String serviceName);
}
