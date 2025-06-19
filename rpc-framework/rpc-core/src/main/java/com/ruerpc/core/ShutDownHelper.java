package com.ruerpc.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Rue
 * @date 2025/6/19 10:45
 */
public class ShutDownHelper {

    //线程安全的挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);

    //挡板关闭期间处理的请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
