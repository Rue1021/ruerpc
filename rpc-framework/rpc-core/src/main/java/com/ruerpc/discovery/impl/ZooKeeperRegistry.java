package com.ruerpc.discovery.impl;

import com.ruerpc.Constant;
import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.ServiceConfig;
import com.ruerpc.discovery.AbstractRegistry;
import com.ruerpc.exceptions.DiscoveryException;
import com.ruerpc.exceptions.NetworkException;
import com.ruerpc.utils.NetUtils;
import com.ruerpc.utils.zookeeper.ZooKeeperNode;
import com.ruerpc.utils.zookeeper.ZooKeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rue
 * @date 2025/5/22 19:00
 *
 * zookeeper注册中心
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
        String node = parentNode + "/" + NetUtils.getIp() + ":" + RueRPCBootstrap.PORT;
        if (!ZooKeeperUtils.exists(zooKeeper, node, null)) {
            ZooKeeperNode zooKeeperNode = new ZooKeeperNode(node, null);
            ZooKeeperUtils.createNode(zooKeeper,zooKeeperNode, null, CreateMode.EPHEMERAL);
        }

        if (log.isDebugEnabled()) {
            log.debug("{}服务被注册", service);
        }
    }

    /**
     * 注册中心的核心作用：获取服务名称，返回服务列表
     * @param serviceName 服务名称
     * @return 服务列表
     */
    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        //1.找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDER_PATH + "/" + serviceName;
        //2.从zk中获取该节点的子节点
        List<String> children = ZooKeeperUtils.getChildren(zooKeeper, serviceNode, null);
        //获取所有可用的服务列表
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();

        if(!(inetSocketAddresses != null && inetSocketAddresses.size() != 0)) {
            throw new DiscoveryException("未发现可用的服务主机");
        }
        return inetSocketAddresses;
    }
}
