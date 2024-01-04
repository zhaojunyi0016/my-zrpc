package com.my.rpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @Author : Williams
 * Date : 2023/12/16 15:30
 */
@Slf4j
@Component
public class RpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        log.debug("provide RpcStarter启动...");
        Thread.sleep(5000);
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
                .scan("com.my.rpc.impl")
                // 启动服务
                .start();
        log.debug("provide RpcStarter完毕...");
        System.out.println("provide RpcStarter完毕...");
    }
}
