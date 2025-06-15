package com.ruerpc;

/**
 * @author Rue
 * @date 2025/5/19 21:30
 */
public interface HelloRueRPC {

    /**
     * 通用接口，client和provider都需要依赖
     * @param msg
     * @return
     */
    String sayHi(String msg);

}
