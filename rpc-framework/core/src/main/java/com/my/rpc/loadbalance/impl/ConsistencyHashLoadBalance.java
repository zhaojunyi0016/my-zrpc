package com.my.rpc.loadbalance.impl;

import com.my.rpc.exception.LoadBalanceException;
import com.my.rpc.loadbalance.AbstractLoadBalancer;
import com.my.rpc.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        private AtomicInteger index;

        public RoundSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNode() {
            if (serviceList == null || serviceList.size() == 0) {
                log.error("进行轮训负载均衡时, 服务列表为空");
                throw new LoadBalanceException("serviceList is null");
            }
            InetSocketAddress address = serviceList.get(index.get());
            if (index.get() == serviceList.size() - 1) {
                // 如果用完了, 重新开始
                index.set(0);
            } else {
                index.incrementAndGet();
            }
            return address;
        }

        @Override
        public void reBalance() {

        }
    }


    public static void main(String[] args) {
        List<InetSocketAddress> serviceList = new ArrayList<>();
        InetSocketAddress ine1 = new InetSocketAddress(8080);
        InetSocketAddress ine2 = new InetSocketAddress(8081);
        InetSocketAddress ine3 = new InetSocketAddress(8082);

        serviceList.add(ine1);
        serviceList.add(ine2);
        serviceList.add(ine3);
        RoundSelector roundSelector = new RoundSelector(serviceList);
        for (int i = 0; i < 11; i++) {
            InetSocketAddress next = roundSelector.getNode();
            System.out.println(next);
        }
    }
}
