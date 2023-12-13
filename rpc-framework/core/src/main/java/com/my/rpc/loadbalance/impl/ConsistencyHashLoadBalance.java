package com.my.rpc.loadbalance.impl;

import com.my.rpc.loadbalance.AbstractLoadBalancer;
import com.my.rpc.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 一致性 hash 负载均衡策略
 *
 * @Author : Williams
 * Date : 2023/12/11 16:04
 */
@Slf4j
public class ConsistencyHashLoadBalance extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundSelector(serviceList);
    }

    /**
     * 选择器 - 具体实现算法
     */
    private static class RoundSelector implements Selector {

        List<InetSocketAddress> serviceList;

        public RoundSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
        }

        @Override
        public InetSocketAddress getNode() {

            return null;
        }

    }


}
