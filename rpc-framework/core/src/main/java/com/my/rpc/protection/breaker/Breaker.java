package com.my.rpc.protection.breaker;

/**
 * @Author : Williams
 * Date : 2023/12/15 11:23
 */
public interface Breaker {
    boolean isBreak();


    /**
     * 记录正常请求数
     */
    void recordRequest();

    /**
     * 记录异常数
     */
    void recordErrorRequest();

    /**
     * 重置熔断器
     */
    void reset();
}
