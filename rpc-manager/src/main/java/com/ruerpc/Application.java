package com.ruerpc;

import com.ruerpc.utils.zookeeper.ZooKeeperNode;
import com.ruerpc.utils.zookeeper.ZooKeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * 注册中心的管理页面
 *
 * @author Rue
 * @date 2025/5/20 14:41
 */
@Slf4j
public class Application {
    public static void main(String[] args) {

            //创建一个zookeeper实例
            ZooKeeper zooKeeper = ZooKeeperUtils.createZookeeper();
            //定义节点和数据
            String basePath = "/ruerpc-metadata";
            String providerPath = basePath + "/providers";
            String consumerPath = basePath + "/consumers";

            ZooKeeperNode baseNode = new ZooKeeperNode(basePath, null);
            ZooKeeperNode providerNode = new ZooKeeperNode(providerPath, null);
            ZooKeeperNode consumerNode = new ZooKeeperNode(consumerPath, null);
            //创建节点
            List.of(baseNode, providerNode, consumerNode).forEach(zooKeeperNode -> {
                ZooKeeperUtils.createNode(zooKeeper, zooKeeperNode, null, CreateMode.PERSISTENT);
            });
            //关闭连接
            ZooKeeperUtils.close(zooKeeper);

    }
}
