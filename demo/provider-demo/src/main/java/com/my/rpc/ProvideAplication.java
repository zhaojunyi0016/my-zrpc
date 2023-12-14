package com.my.rpc;

import com.my.rpc.impl.HelloRpcImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务提供方
 *
 * @Author : Williams
 * Date : 2023/12/4 18:41
 */
@Slf4j
public class ProvideAplication {

    public static void main(String[] args) {
        log.debug("provide start....");

        // 定义具体的服务
        ServiceConfig<SayHelloRpc> service = new ServiceConfig<>();
        service.setInterface(SayHelloRpc.class);
        service.setRef(new HelloRpcImpl());

        /*
         * 1. 封装要发布的服务
         * 2. 定义注册中心
         * 3. 通过引导程序, 启动服务提供方
         * 配置--应用的名称  --注册中心
         * 发布服务
         */
        RpcBootstrap.getInstance()
                // 配置注册中心
                .registry()
                // 扫包发布服务
                .scan("com.my.rpc")
                // 启动服务
                .start();
    }
}
