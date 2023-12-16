package com.my.rpc.impl;

import com.my.rpc.SayHelloRpc2;
import com.my.rpc.annotation.RpcApi;

/**
 * @Author : Williams
 * Date : 2023/12/4 17:40
 */
@RpcApi
public class HelloRpcImpl2 implements SayHelloRpc2 {
    @Override
    public String sayHi(String msg) {
        return "你好2 + consumer:" + msg;
    }
}
