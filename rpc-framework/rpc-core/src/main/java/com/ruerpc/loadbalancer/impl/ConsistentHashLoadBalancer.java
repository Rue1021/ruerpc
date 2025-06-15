package com.ruerpc.loadbalancer.impl;

import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.loadbalancer.AbstractLoadBalancer;
import com.ruerpc.loadbalancer.ServiceSelector;
import com.ruerpc.transport.message.RueRPCRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Rue
 * @date 2025/6/11 16:20
 *
 * 一致性hash负载均衡
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected ServiceSelector getServiceSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    /**
     * 一致性hash的具体算法实现
     */
    private static class ConsistentHashSelector implements ServiceSelector {

        private SortedMap<Integer, InetSocketAddress> hashCircle = new TreeMap<>();
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList,
                                      int virtualNodes) {
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress address : serviceList) {
                addNode2Circle(address);
            }
        }



        @Override
        public InetSocketAddress getNext() {
            RueRPCRequest rueRPCRequest = RueRPCBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = Long.toString(rueRPCRequest.getRequestId());
            int hash = myHash(requestId);
            if (!hashCircle.containsKey(hash)) {
                //当前请求的hash值没有正好落在服务器上，就把此请求转发至离这个hash值最近的虚拟节点
                SortedMap<Integer, InetSocketAddress> tailMap = hashCircle.tailMap(hash);
                /*
                1.当子树tailMap为空，即这个hash值大于树里所有虚拟节点的hash值，
                所以把这个值坐落到哈希环的firstKey()，代表红黑树里最左的、最小的hash值.
                2.如果非空，则子树的firstKey()是小于此hash值的最近的虚拟节点
                 */
                hash = tailMap.isEmpty() ? hashCircle.firstKey() : tailMap.firstKey();

            }
            return hashCircle.get(hash);
        }


        /**
         * 将每个节点挂载到hash还上
         * @param address 节点的地址
         */
        private void addNode2Circle(InetSocketAddress address) {
            //为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = myHash(address.toString() + "-" + i);
                hashCircle.put(hash, address);
                if (log.isDebugEnabled()) {
                    log.debug("hash为【{}】的节点已经挂载到hash环上", hash);
                }
            }
        }

        /**
         * 删除节点
         * @param address
         */
        private void removeNodeFromCircle(InetSocketAddress address) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = myHash(address.toString() + "-" + i);
                hashCircle.remove(hash, address);
            }
        }

        private int myHash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());

            //md5可以得到一个字节数组，但是我们想要int -> 4字节
            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if (digest[i] < 0) {
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;
        }
    }
}
