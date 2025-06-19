package com.ruerpc.watcher;

import com.ruerpc.NettyBootstrapInitializer;
import com.ruerpc.RueRPCBootstrap;
import com.ruerpc.discovery.Registry;
import com.ruerpc.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author Rue
 * @date 2025/6/15 09:31
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            if (log.isDebugEnabled()) {
                log.debug("检测到【{}】服务下有有节点上下线，将重新拉取服务列表", watchedEvent.getPath());
            }
            //拿到注册中心
            Registry registry = RueRPCBootstrap.getInstance()
                    .getConfiguration().getRegistryConfig().getRegistry();
            String serviceName = getServiceName(watchedEvent.getPath());
            List<InetSocketAddress> addresses = registry.lookup(serviceName,
                    RueRPCBootstrap.getInstance().getConfiguration().getGroup());
            //处理新增节点：根据地址建立连接并缓存
            for (InetSocketAddress address : addresses) {
                if (!RueRPCBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    try {
                        Channel channel = NettyBootstrapInitializer.getBootstrap().connect().sync().channel();
                        RueRPCBootstrap.CHANNEL_CACHE.put(address, channel);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            //处理下线节点：下线的节点可能在CHANNEL_CACHE里，但一定不在刚拉取的addresses里
            for (Map.Entry<InetSocketAddress, Channel> entry : RueRPCBootstrap.CHANNEL_CACHE.entrySet()) {
                if (!addresses.contains(entry.getKey())) {
                    RueRPCBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            //拿到负载均衡器
            LoadBalancer loadBalancer = RueRPCBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName, addresses);
        }
    }

    private String getServiceName(String patch) {
        String[] split = patch.split("/");
        return split[split.length - 1];
    }
}
