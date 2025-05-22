package com.ruerpc.discovery.impl;

import com.ruerpc.Constant;
import com.ruerpc.ServiceConfig;
import com.ruerpc.discovery.AbstractRegistry;
import com.ruerpc.utils.NetUtils;
import com.ruerpc.utils.zookeeper.ZooKeeperNode;
import com.ruerpc.utils.zookeeper.ZooKeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author Rue
 * @date 2025/5/22 19:00
 */
@Slf4j
public class ZooKeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper = ZooKeeperUtils.createZookeeper();

    public ZooKeeperRegistry() {

    }

    public ZooKeeperRegistry(String host, int timeout) {

    }

    @Override
    public void register(ServiceConfig<?> service) {
        //节点的服务名称，这是个持久节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        if (!ZooKeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZooKeeperNode zooKeeperNode = new ZooKeeperNode(parentNode, null);
            ZooKeeperUtils.createNode(zooKeeper,zooKeeperNode, null, CreateMode.PERSISTENT);
        }

        //创建本机的临时节点 ip:port
        //ip通常需要一个局域网ip
        //服务提供方的端口一般自己设定
        //TODO后续处理端口
        String node = parentNode + "/" + NetUtils.getIp() + ":" + 8090;
        if (!ZooKeeperUtils.exists(zooKeeper, node, null)) {
            ZooKeeperNode zooKeeperNode = new ZooKeeperNode(node, null);
            ZooKeeperUtils.createNode(zooKeeper,zooKeeperNode, null, CreateMode.EPHEMERAL);
        }

        if (log.isDebugEnabled()) {
            log.debug("{}服务被注册", service);
        }
    }
}
