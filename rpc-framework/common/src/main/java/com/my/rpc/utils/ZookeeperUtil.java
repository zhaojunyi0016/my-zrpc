package com.my.rpc.utils;

import com.my.rpc.exception.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.my.rpc.constant.Constant.DEFAULT_ZK_CONNECT;
import static com.my.rpc.constant.Constant.DEFAULT_ZK_TIMEOUT;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:00
 */
@Slf4j
public class ZookeeperUtil {


    public static ZooKeeper create() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            ZooKeeper zooKeeper = new ZooKeeper(DEFAULT_ZK_CONNECT, DEFAULT_ZK_TIMEOUT, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.None) {
                    if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        log.debug("获取 zk 连接成功..");
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (Exception e) {
            e.printStackTrace();
            countDownLatch.countDown();
            throw new ZookeeperException();
        }
    }

    /**
     * 判断节点是否存在
     *
     * @param zooKeeper zk 实例
     * @param watcher   watcher
     * @param nodePath  节点路径
     * @return true 存在, false 不存在
     */
    public static boolean exists(ZooKeeper zooKeeper, Watcher watcher, String nodePath) {
        try {
            return zooKeeper.exists(nodePath, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createNode(ZooKeeper zooKeeper, Watcher watcher, CreateMode createMode, ZookeeperNode... zookeeperNodes) {
        try {
            for (ZookeeperNode zookeeperNode : zookeeperNodes) {
                Stat exists = zooKeeper.exists(zookeeperNode.getNodePath(), watcher);
                if (exists == null) {
                    zooKeeper.create(zookeeperNode.getNodePath(), null,
                            ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                } else {
                    log.warn("节点已存在 exists ={}", exists);
                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            throw new ZookeeperException();
        }
    }


    /**
     * 获取当前节点的子节点
     *
     * @param zooKeeper       zk 实例
     * @param serviceNodePath 当前节点路径
     * @return 子节点 list
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNodePath, Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNodePath, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取节点的子节点发生异常, node  = {}, error ={}", serviceNodePath, e);
            throw new ZookeeperException();
        }
    }
}
