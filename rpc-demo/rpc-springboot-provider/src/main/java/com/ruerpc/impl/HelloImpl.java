package com.ruerpc.impl;

import com.ruerpc.Hello;
import com.ruerpc.annotation.RueRPCApi;

import java.util.concurrent.CompletableFuture;

/**
 * @author Rue
 * @date 2025/5/19 21:35
 */
@RueRPCApi(group = "primary")
public class HelloImpl implements Hello {

//    @Override
//    public String sayHi(String msg) {
//        return "hi consumer " + msg;
//    }

    @Override
    public CompletableFuture<String> sayHi(String name) {
        // 方案1：直接返回已完成的Future（适用于简单逻辑）
        return CompletableFuture.completedFuture("hi consumer " + name);
    }
}
