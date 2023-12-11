package com.my.rpc.discovery.impl;

import com.my.rpc.ServiceConfig;
import com.my.rpc.discovery.AbstractRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * zookeeper 注册中心
 *
 * @Author : Williams
 * Date : 2023/12/6 16:46
 */
@Slf4j
public class EurekaRegistry extends AbstractRegistry {

    public EurekaRegistry() {
        log.debug("获取 eureka 注册中心");
    }

    @Override
    public void publish(ServiceConfig<?> service) {
    }

    @Override
    public List<InetSocketAddress> lookup(String name) {
        return null;
    }
}
