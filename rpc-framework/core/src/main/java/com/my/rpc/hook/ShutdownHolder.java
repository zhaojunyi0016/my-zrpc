package com.my.rpc.hook;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author : Williams
 * Date : 2023/12/16 10:27
 */
public class ShutdownHolder {

    // 标记请求挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);

    // 请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
