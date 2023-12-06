package com.my.rpc;

import com.my.rpc.utils.ZookeeperNode;
import com.my.rpc.utils.ZookeeperUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

/**
 * 注册中心的管理页面
 *
 * @Author : Williams
 * Date : 2023/12/5 17:14
 */
public class ManagerApplication {

    public static void main(String[] args) {
        // 帮我们创建基础目录
        ZooKeeper zooKeeper = ZookeeperUtil.create();

        // 定义节点
        ZookeeperNode baseNode = new ZookeeperNode("/myrpc-metaData", null);
        ZookeeperNode provideNode = new ZookeeperNode(baseNode.getNodePath() + "/provide", null);
        ZookeeperNode consumerNode = new ZookeeperNode(baseNode.getNodePath() + "/consumer", null);

        // 创建节点
        ZookeeperUtil.createNode(zooKeeper, null, CreateMode.PERSISTENT, baseNode, provideNode, consumerNode);
    }

}
