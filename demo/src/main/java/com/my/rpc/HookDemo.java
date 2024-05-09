package com.my.rpc;

/**
 * @Author : Williams
 * Date : 2023/12/15 17:51
 */
public class HookDemo {

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("程序正在关闭..");
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("请求已经被处理完成");
        }));

        while (true) {
            Thread.sleep(100);
            System.out.println("正在处理请求");
        }
    }
}
