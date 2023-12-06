package com.my.rpc;

import com.my.rpc.protocol.ProtocolConfig;
import com.my.rpc.register.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author : Williams
 * Date : 2023/12/5 15:00
 */
public class RpcBootstrap {

    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

    private RpcBootstrap() {
        // 构建启动引导程序时, 初始化相关属性
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
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registryConfig
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前配置服务的序列化协议
     *
     * @param protocolConfig 协议的封装
     * @return this
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        System.out.println("当前工程使用了 " + protocolConfig.toString() + "协议进行序列化");
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        System.out.println("项目启动... ");
    }


    //--------------------------------服务提供方的 api----------------------------------------

    /**
     * 发布服务 - 将匹配的接口, 注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        System.out.println("服务" + service.getInterface().toString() + " , 已经被注册... ");
        return this;
    }

    /**
     * 批量发布服务
     *
     * @param services 封装的需要发布的服务
     * @return this
     */
    public RpcBootstrap publish(List<?> services) {

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
        return this;
    }


    //--------------------------------服务调用方的 api-----------------------------------------

}
