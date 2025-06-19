package com.ruerpc.impl;

import com.ruerpc.HelloRueRPC;
import com.ruerpc.annotation.RueRPCApi;

/**
 * @author Rue
 * @date 2025/5/19 21:35
 */
@RueRPCApi
public class HelloRueRPCImpl implements HelloRueRPC {

    @Override
    public String sayHi(String msg) {
        return "hi consumer " + msg;
    }
}
