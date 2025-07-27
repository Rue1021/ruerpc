package com.ruerpc.protection;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rue
 * @date 2025/6/18 15:15
 *
 * 一个简单的熔断器实现，只有 开 闭 两种状态
 */
public class CircuitBreaker {

    //默认情况下，熔断器是不打开的
    private volatile boolean isWorking = false;

    // 使用全局单例Timer来完成熔断器重置（注意线程安全）
    public static final Timer RESET_TIMER = new Timer("CircuitBreaker-Reset-Timer", true);


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
    public boolean isWorking() {
        //如果熔断器是工作状态，返回true，表示熔断器isWorking
        if (isWorking) return true;

        //失败请求超过阈值
        if (errorRequestCount.get() >= maxErrorRequestCount ) {
            this.isWorking = true;
            return true;
        }

        //失败率超过阈值
        if (requestCount.get() > 0 && errorRequestCount.get() > 0
        && errorRequestCount.get() / (float) maxErrorRequestCount >= maxErrorRate) {
            this.isWorking = true;
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
        this.isWorking = false;
        this.requestCount.set(0);
        this.errorRequestCount.set(0);
    }
}
