package com.ruerpc;

import com.ruerpc.annotation.TryTimes;

/**
 * @author Rue
 * @date 2025/5/19 21:30
 */
public interface Hello {

    /**
     * 通用接口，client和provider都需要依赖
     * @param msg
     * @return
     */
    @TryTimes(tryTimes = 3, intervalTime = 3000)
    String sayHi(String msg);

}
