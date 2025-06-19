package com.ruerpc.impl;

import com.ruerpc.Hello;
import com.ruerpc.annotation.RueRPCApi;

/**
 * @author Rue
 * @date 2025/5/19 21:35
 */
@RueRPCApi(group = "primary")
public class HelloImpl implements Hello {

    @Override
    public String sayHi(String msg) {
        return "hi consumer " + msg;
    }
}
