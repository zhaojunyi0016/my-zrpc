package com.my.rpc.discovery;

import com.my.rpc.discovery.impl.ZookeeperRegistry;
import com.my.rpc.exception.RegistryException;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册中心配置类
 *
 * @Author : Williams
 * Date : 2023/12/5 15:18
 */

@Data
@NoArgsConstructor
public class RegistryConfig {

    private String connectString;
    private String registryCode;


    public RegistryConfig(String registryCode, String connectString) {
        this.registryCode = registryCode;
        this.connectString = connectString;
    }


    /**
     * 简单工厂
     * 通过 registryCode 获取一个具体的注册中心
     *
     * @param registryCode 注册中心类型
     * @return 具体的注册中心
     */
    public Registry getRegistryByCode(RegistryConfig registryCode) {
        switch (registryCode.getRegistryCode()) {
            case "zookeeper":
                return new ZookeeperRegistry();
            case "nacos":
                return new ZookeeperRegistry();
            case "eureka":
                return new ZookeeperRegistry();
            default:
                throw new RegistryException("未找到注册中心");
        }
    }
}
