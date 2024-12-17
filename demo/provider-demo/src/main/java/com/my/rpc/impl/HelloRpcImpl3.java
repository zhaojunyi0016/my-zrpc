package com.my.rpc.impl;

import com.my.rpc.SayHelloRpc3;

/**
 * @Author : Williams
 * Date : 2023/12/4 17:40
 */
public class HelloRpcImpl3 implements SayHelloRpc3 {
    @Override
    public String sayHi(String msg) {
        return "23你好 + consumer:" + msg;
    }
}
