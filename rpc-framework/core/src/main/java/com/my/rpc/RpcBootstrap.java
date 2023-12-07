package com.my.rpc;

import com.my.rpc.discovery.Registry;
import com.my.rpc.discovery.RegistryConfig;
import com.my.rpc.protocol.ProtocolConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : Williams
 * Date : 2023/12/5 15:00
 */
@Slf4j
public class RpcBootstrap {

    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

    // 默认名称
    private String appName = "defalut";

    // 注册中心
    private RegistryConfig registryConfig;

    // 序列化协议
    private ProtocolConfig protocolConfig;

    // 注册中心
    private Registry registry;

    // 维护已经发布的服务列表  key -> interface全限定名称
    private static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>();

    private RpcBootstrap() {
    }

    /**
     * 创建实例 - 单例模式
     *
     * @return RpcBootstrap
     */
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * 定义当前应用的名字
     *
     * @param appName
     * @return this
     */
    public RpcBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registryConfig
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
        this.registry = registryConfig.getRegistryByCode(registryConfig);
        return this;
    }

    /**
     * 配置当前配置服务的序列化协议
     *
     * @param protocolConfig 协议的封装
     * @return this
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        log.debug("当前工程使用了  {}   协议进行序列化", protocolConfig.getProtocolName());
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        log.debug("项目启动");
        try {
            Thread.sleep(1000000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.warn("项目关闭");
    }


    //--------------------------------服务提供方的 api----------------------------------------

    /**
     * 发布服务 - 将匹配的接口, 注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this 当前实例
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        // 使用注册中心 发布对应接口
        registry.publish(service);

        // 1. 当服务调用方, 通过接口, 方法名, 具体的方法参数 发起调用, 提供方怎么知道使用哪个实现
        // (1) new 一个  (2) 通过 spring beanFactory.getBean(Class)   (3) 自己维护映射关系
        SERVICE_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     *
     * @param services 封装的需要发布的服务
     * @return this
     */
    public RpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    //--------------------------------服务提供方的 api-----------------------------------------


    //--------------------------------服务调用方的 api-----------------------------------------

    /**
     * @return
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是香可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        reference.setRegistry(registry);
        return this;
    }


    //--------------------------------服务调用方的 api-----------------------------------------

}
