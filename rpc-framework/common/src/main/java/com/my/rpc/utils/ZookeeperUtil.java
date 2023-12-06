package com.my.rpc.utils;

import com.my.rpc.exception.ZookeeperException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

import static com.my.rpc.constant.Constant.DEFAULT_ZK_CONNECT;
import static com.my.rpc.constant.Constant.DEFAULT_ZK_TIMEOUT;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:00
 */
public class ZookeeperUtil {


    public static ZooKeeper create() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            ZooKeeper zooKeeper = new ZooKeeper(DEFAULT_ZK_CONNECT, DEFAULT_ZK_TIMEOUT, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.None) {
                    if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        System.out.println("连接成功");
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


    public static void createNode(ZooKeeper zooKeeper, Watcher watcher, CreateMode createMode, ZookeeperNode... zookeeperNodes) {
        try {
            for (ZookeeperNode zookeeperNode : zookeeperNodes) {
                Stat exists = zooKeeper.exists(zookeeperNode.getNodePath(), watcher);
                if (exists == null) {
                    zooKeeper.create(zookeeperNode.getNodePath(), null,
                            ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                } else {
                    System.out.println("节点已存在 exists = " + exists);
                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            throw new ZookeeperException();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
