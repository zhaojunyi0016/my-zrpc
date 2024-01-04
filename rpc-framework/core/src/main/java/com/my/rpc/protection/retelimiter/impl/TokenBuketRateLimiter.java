package com.my.rpc.protection.retelimiter.impl;

import com.my.rpc.protection.retelimiter.ReteLimiter;

/**
 * 令牌桶限流器
 * rate 令牌桶的速率表示令牌产生速率
 * capacity 容量表示令牌桶容量。
 * tokens 标识可用的令牌数
 * lastAddTokenTime 标识最后一次添加令牌时间, 添加逻辑是每次获取当前时间, 跟上一次添加令牌时间对比, 超过 1 秒的则按照速率添加 N 个令牌
 * 通过allowRequest函数可以获取一定数量的令牌，如果够直接拿, 不够返回 false
 * 如果令牌数量足够，则表示可以通过流量控制，否则表示流量控制未通过。
 *
 * @Author : Williams
 * Date : 2023/12/14 18:15
 */
public class TokenBuketRateLimiter implements ReteLimiter {
    // 令牌, 大于 0 就能放行,并且 -1,  等于 0 : 无令牌
    private static int tokens;

    // 总容量
    private final int capacity;

    // 速率: 每秒给桶加 N 个, 不能超过总数
    private final int rate;

    // 最后一次添加令牌时间
    private long lastAddTokenTime;

    public TokenBuketRateLimiter(int capacity, int rate) {
        this.rate = rate;
        this.capacity = capacity;
        lastAddTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }

    public synchronized boolean allowRequest() {
        // 1. 给令牌桶添加令牌
        long currentTime = System.currentTimeMillis();
        // 计算现在和上一次的添加令牌的时间间隔
        long gap = currentTime - lastAddTokenTime;
        // 间隔时间大于 1 秒, 放入新的令牌
        if (gap >= 1000) {
            int needAddTokens = (int) (gap * rate / 1000);
            // 给令牌桶 添加令牌
            tokens = Math.min(capacity, tokens + needAddTokens);
            // 标记最后一次添加令牌的时间
            this.lastAddTokenTime = System.currentTimeMillis();
        }

        // 2. 获取令牌, 如果有, 则放行, 否则拦截
        if (tokens > 0) {
            tokens--;
            return true;
        } else {
            return false;
        }

    }
}
