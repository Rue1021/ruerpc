package com.ruerpc.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Rue
 * @date 2025/6/11 14:05
 */
public interface ServiceSelector {

    /**
     * 执行一种算法，从服务列表获取一个可用服务节点
     * @param
     * @return
     */
    InetSocketAddress getNext();

}
