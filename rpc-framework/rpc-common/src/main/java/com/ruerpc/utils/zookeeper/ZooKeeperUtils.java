package com.ruerpc.utils.zookeeper;

import com.ruerpc.Constant;
import com.ruerpc.exceptions.ZooKeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author Rue
 * @date 2025/5/22 15:06
 */
@Slf4j
public class ZooKeeperUtils {

    /**
     * 使用默认配置创建zookeeper实例
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper() {

        //连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT_ADDR;
        //定义超时时间
        int timeout = Constant.TIMEOUT;

        return createZookeeper(connectString, timeout);

    }

    public static ZooKeeper createZookeeper(String connectString, int timeout) {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            log.info("尝试建立zookeeper连接");
            //创建zookeeper实例，建立连接
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, event -> {
                //只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();

            return zooKeeper;

        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例时产生异常，异常信息如下：", e);
            throw new ZooKeeperException();
        }
    }

    /**
     * 创建一个节点的工具方法
     * @param zooKeeper
     * @param zooKeeperNode
     * @param watcher
     * @param createMode
     * @return true: 成功创建 false: 节点已经存在 异常: 抛出
     */
    public static Boolean createNode(ZooKeeper zooKeeper,
                                       ZooKeeperNode zooKeeperNode,
                                       Watcher watcher,
                                       CreateMode createMode) {
        try {
            if (zooKeeper.exists(zooKeeperNode.getNodePath(), watcher) == null) {
                String s = zooKeeper.create(zooKeeperNode.getNodePath(), zooKeeperNode.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点【{}】成功创建", s);
                return Boolean.TRUE;
            } else {
                if (log.isDebugEnabled()) {
                    log.info("节点【{}】已经存在", zooKeeperNode.getNodePath());
                }
            }
            return Boolean.FALSE;
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时产生异常，异常信息如下：", e);
            throw new ZooKeeperException();
        }
    }

    /**
     * 判断节点是否存在
     * @param zooKeeper
     * @param zooKeeperNode
     * @param watcher
     * @return true: 节点存在
     */
    public static boolean exists(ZooKeeper zooKeeper, String zooKeeperNode, Watcher watcher) {
        try {
            return zooKeeper.exists(zooKeeperNode, watcher) != null;

        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点{}是否存在时发生异常:", zooKeeperNode, e);
            throw new ZooKeeperException(e);
        }
    }

    /**
     * 关闭zookeeper的方法
     * @param zooKeeper
     */
    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时产生异常，异常信息如下：", e);
            throw new ZooKeeperException();
        }
    }


}
