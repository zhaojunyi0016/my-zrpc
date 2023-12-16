package com.my.rpc.impl;

import com.my.rpc.SayHelloRpc;
import com.my.rpc.annotation.RpcApi;

/**
 * @Author : Williams
 * Date : 2023/12/4 17:40
 */
@RpcApi
public class HelloRpcImpl implements SayHelloRpc {
    @Override
    public String sayHi(String msg) {
        return "你好 + consumer:" + msg;
    }
}
