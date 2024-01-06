package com.my.rpc.api;

import com.my.rpc.SayHelloRpc;
import com.my.rpc.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author : Williams
 * Date : 2023/12/16 12:05
 */
@RestController
public class TestController {

    @RpcReference
    private SayHelloRpc sayHelloRpc;

    @GetMapping("testConsumer")
    public String test() {
        return "你好, consumer";
    }

    @GetMapping("hello")
    public String hello() {
        return sayHelloRpc.sayHi("我的大佬");
    }
}
