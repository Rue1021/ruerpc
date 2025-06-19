package com.ruerpc.protection;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rue
 * @date 2025/6/18 15:15
 *
 * 一个简单的熔断器实现，只有 开 闭 两种状态
 */
public class CircuitBreaker {

    //默认情况下，熔断器是不打开的
    private volatile boolean isOpen = false;

    //总请求数
    private AtomicInteger requestCount = new AtomicInteger(0);
    //异常的请求数
    private AtomicInteger errorRequestCount = new AtomicInteger(0);
    //异常请求的阈值
    private int maxErrorRequestCount;
    private float maxErrorRate;

    public CircuitBreaker(int maxErrorRequestCount, float maxErrorRate) {
        this.maxErrorRequestCount = maxErrorRequestCount;
        this.maxErrorRate = maxErrorRate;
    }

    /**
     * 熔断器的核心方法
     * @return
     */
    public boolean isBreak() {
        if (isOpen) return true;

        if (errorRequestCount.get() >= maxErrorRequestCount ) {
            this.isOpen = true;
            return true;
        }

        if (requestCount.get() > 0 && errorRequestCount.get() > 0
        && errorRequestCount.get() / (float) maxErrorRequestCount >= maxErrorRate) {
            this.isOpen = true;
            return true;
        }
        return false;
    }

    public void recordRequest() {
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest() {
        this.errorRequestCount.getAndIncrement();
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequestCount.set(0);
    }
}
