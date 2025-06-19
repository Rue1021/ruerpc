package com.ruerpc.core;

import java.util.concurrent.TimeUnit;

/**
 * @author Rue
 * @date 2025/6/19 10:40
 */
public class RueRPCShutdownHook extends Thread {

    @Override
    public void run() {
        //1. 打开挡板
        ShutDownHelper.BAFFLE.set(true);
        //2. 等待计数器归0
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //计数器归0 (请求全部处理完) 或 等待10秒后仍未处理完 就关闭
            if (ShutDownHelper.REQUEST_COUNTER.sum() == 0L
            || System.currentTimeMillis() - start >= 10000) {
                break;
            }
        }
        //3. 阻塞结束后放行，执行释放资源等操作
    }
}
