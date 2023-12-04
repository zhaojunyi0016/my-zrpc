package com.my.rpc.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * @Author : Williams
 * Date : 2023/12/2 10:07
 */
public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.None){
            if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("连接成功");
            }
        }
    }
}
