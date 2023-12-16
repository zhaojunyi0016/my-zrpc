package com.my.rpc.discovery.impl;

import com.my.rpc.RpcBootstrap;
import com.my.rpc.ServiceConfig;
import com.my.rpc.constant.Constant;
import com.my.rpc.discovery.AbstractRegistry;
import com.my.rpc.exception.NetException;
import com.my.rpc.utils.NetUtil;
import com.my.rpc.utils.ZookeeperNode;
import com.my.rpc.utils.ZookeeperUtil;
import com.my.rpc.watch.UpAndDownWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * zookeeper 注册中心
 *
 * @Author : Williams
 * Date : 2023/12/6 16:46
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private final ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        log.debug("获取 zooKeeper 注册中心");
        this.zooKeeper = ZookeeperUtil.create();
    }

    @Override
    public void publish(ServiceConfig<?> service) {
        // 服务名称的节点, 并且应该是一个持久节点
        String parentNode = Constant.PROVIDE_PATH + "/" + service.getInterface().getName();
        ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
        ZookeeperUtil.createNode(zooKeeper, null, CreateMode.PERSISTENT, zookeeperNode);
        log.debug("服务 {} , 已经被注册... ", service.getInterface().getName());

        // 服务提供方的端口, 一般自己设定
        // ip 需要局域网 ip, 不是 127.0.0.1. 也不是 ipv6
        String node = parentNode + "/" + NetUtil.getIp() + ":" + RpcBootstrap.getInstance().getConfiguration().getPort();
        zookeeperNode = new ZookeeperNode(node, null);
        ZookeeperUtil.createNode(zooKeeper, null, CreateMode.EPHEMERAL, zookeeperNode);
    }


    /**
     * 拉取服务列表
     *
     * @param serviceName 方法的全限定名
     * @return
     */
    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        // 找到服务对应的节点
        String serviceNodePath = Constant.PROVIDE_PATH + "/" + serviceName;

        // 从 zk 中获取他的子节点   如果children 有问题, watch机制干活
        List<String> children = ZookeeperUtil.getChildren(zooKeeper, serviceNodePath, new UpAndDownWatch());

        // 获取了所有的可用的服务列表
        List<InetSocketAddress> inetSocketAddressList = children.stream().map(ipString -> {
            String[] split = ipString.split(":");
            return new InetSocketAddress(split[0], Integer.valueOf(split[1]));
        }).collect(Collectors.toList());

        if (inetSocketAddressList.size() > 0) {
            return inetSocketAddressList;
        } else {
            throw new NetException();
        }
    }
}
