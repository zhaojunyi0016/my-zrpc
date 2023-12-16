package com.my.rpc.hook;

/**
 * @Author : Williams
 * Date : 2023/12/16 10:20
 */
public class RpcShutdownHook extends Thread {

    @Override
    public void run() {
        // 1. 打开挡板
        ShutdownHolder.BAFFLE.set(true);
        // 2. 等待计数器归零
        long start = System.currentTimeMillis();
        while (true) {
            System.out.println("请求正在被处理--------");
            long sum = ShutdownHolder.REQUEST_COUNTER.sum();
            System.out.println("sum = " + sum);
            if (sum == 0L
                    || System.currentTimeMillis() - start > 20000L) {
                System.out.println("请求处理完毕--------");
                break;
            }
        }
        // 3. 放行
    }
}
