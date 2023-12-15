package com.my.rpc.protection.breaker.impl;

import com.my.rpc.protection.breaker.Breaker;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 熔断器
 *
 * @Author : Williams
 * Date : 2023/12/15 10:43
 */
public class CircuitBreaker implements Breaker {

    // 总的请求数
    private final AtomicInteger requestConut = new AtomicInteger(0);
    // 异常的请求书
    private final AtomicInteger errorRequestCount = new AtomicInteger(0);
    // 允许最大的异常比例
    private final int maxErrorRequest;
    private final float maxErrorRate;
    // 熔断器有 3 种状态 open close half_open
    private volatile boolean isOpen = false;


    public CircuitBreaker(int maxErrorRequest, float maxErrorRate) {
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }

    public static void main(String[] args) throws InterruptedException {
        CircuitBreaker circuitBreaker = new CircuitBreaker(3, 1.1f);

        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                circuitBreaker.recordRequest();
                int num = new Random().nextInt(100);
                if (num > 70) {
                    circuitBreaker.recordErrorRequest();
                }

                boolean aBreak = circuitBreaker.isBreak();
                String result = aBreak ? "断路器阻塞了请求" : "断路器放行了请求";
                System.out.println(result);
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000L);
                    circuitBreaker.reset();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        Thread.sleep(10000L);

    }

    public boolean isBreak() {
        // 如果是打开的, 则返回一个 true
        if (isOpen) {
            return true;
        }
        // 达到最大的失败次数
        if (errorRequestCount.get() > maxErrorRequest) {
            this.isOpen = true;
            return true;
        }
        // 达到了失败率
        if (errorRequestCount.get() > 0 && requestConut.get() > 0 &&
                errorRequestCount.get() / (float) requestConut.get() > maxErrorRate) {
            this.isOpen = true;
            return true;
        }
        return false;
    }

    /**
     * 记录正常请求数
     */
    public void recordRequest() {
        this.requestConut.getAndIncrement();
    }

    /**
     * 记录异常数
     */
    public void recordErrorRequest() {
        this.errorRequestCount.getAndIncrement();
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        this.isOpen = false;
        this.requestConut.set(0);
        this.errorRequestCount.set(0);
    }
}
