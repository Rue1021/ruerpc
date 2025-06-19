package com.ruerpc.protection;

/**
 * @author Rue
 * @date 2025/6/18 20:09
 */
public interface RateLimiter {

    /**
     * 是否允许放行新的请求
     * @return true 可以放行 false 拦截
     */
    boolean allowRequest();
}
