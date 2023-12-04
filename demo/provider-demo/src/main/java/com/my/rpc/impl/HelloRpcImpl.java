package com.my.rpc.impl;

import com.my.rpc.SayHelloRpc;

/**
 * @Author : Williams
 * Date : 2023/12/4 17:40
 */
public class HelloRpcImpl implements SayHelloRpc {
    @Override
    public String sayHi(String msg) {
        return "hi + consumer:" + msg;
    }
}
