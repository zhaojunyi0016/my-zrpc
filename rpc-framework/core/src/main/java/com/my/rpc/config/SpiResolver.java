package com.my.rpc.config;

import com.my.rpc.compress.Compressor;
import com.my.rpc.loadbalance.LoadBalancer;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.serialize.SerializerFactory;
import com.my.rpc.spi.SpiHandler;

/**
 * SPI 解析器
 *
 * @Author : Williams
 * Date : 2023/12/14 14:27
 */
public class SpiResolver {

    /**
     * 通过 SPI 的方式加载配置项
     *
     * @param configuration
     */
    public static void loadFromSpi(Configuration configuration) {

        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        if (loadBalancer != null) {
            configuration.setLoadBalancer(loadBalancer);
        }

    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }
}
