package com.my.rpc.loadbalance;

import java.net.InetSocketAddress;

/**
 * @Author : Williams
 * Date : 2023/12/11 16:10
 */
public interface Selector {

    /**
     * 根据服务列表, 执行对应算法, 返回一个服务节点
     *
     * @return 返回一个服务节点
     */
    InetSocketAddress getNode();


}
