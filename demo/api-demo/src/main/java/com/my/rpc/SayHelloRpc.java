package com.my.rpc;

import com.my.rpc.annotation.TryTimes;

/**
 * @Author : Williams
 * Date : 2023/12/4 17:15
 */
public interface SayHelloRpc {

    /**
     * 通用接口 , server 和 client 都需要依赖
     *
     * @param msg 发送的消息
     * @return 返回的消息
     */
    @TryTimes(tryTimes = 3)
    String sayHi(String msg);
}
