package com.my.rpc;

/**
 * @Author : Williams
 * Date : 2023/12/4 17:15
 */
public interface SayHelloRpc2 {

    /**
     * 通用接口 , server 和 client 都需要依赖
     *
     * @param msg 发送的消息
     * @return 返回的消息
     */
    String sayHi(String msg);
}
