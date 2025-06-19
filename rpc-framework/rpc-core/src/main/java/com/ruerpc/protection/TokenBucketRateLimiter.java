package com.ruerpc.protection;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Rue
 * @date 2025/6/18 11:09
 *
 * 基于令牌桶算法的限流器
 */
@Slf4j
public class TokenBucketRateLimiter implements RateLimiter {

    private int tokens;

    private final int capacity;

    //添加令牌的速率 -- 每秒添加的令牌数
    private final int rate;

    private Long lastTokenTime;

    public TokenBucketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = 0;
    }

    /**
     * 判断请求是否可以放行
     * @return true 放行 false 拦截
     */
    public synchronized boolean allowRequest() {

        //1. 给令牌桶添加令牌
        Long currentTime = System.currentTimeMillis();
        Long timeInterval = currentTime - lastTokenTime;
        if (timeInterval >= 1000 / rate) {
            int tokensNeeded2Add = (int) (timeInterval * rate / 1000);
            tokens = Math.min(capacity, tokens + tokensNeeded2Add);
            //记录上次放令牌的时间
            this.lastTokenTime = System.currentTimeMillis();
        }

        //2. 自己获取令牌
        if (tokens <= 0) {
            log.error("请求被拦截");
            return false;
        }
        tokens--;
        return true;
    }
}
