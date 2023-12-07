package com.my.rpc.netty;

import java.util.List;
import java.util.concurrent.*;

/**
 * @Author : Williams
 * Date : 2023/12/7 15:19
 */
public class DemoCompletableFutureFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> objectCompletableFuture = new CompletableFuture<>();
        new Thread(() -> {
            int i = 9;
            objectCompletableFuture.complete(i);
        }).start();
        // 如何在 main 线程中获取这个9
        Integer integer = objectCompletableFuture.get();
        System.out.println(integer);
    }
}
