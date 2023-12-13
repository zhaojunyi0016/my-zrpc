package com.my.rpc.loadbalance.impl;

import com.my.rpc.RpcBootstrap;
import com.my.rpc.loadbalance.AbstractLoadBalancer;
import com.my.rpc.loadbalance.Selector;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.TreeMap;

/**
 * 最小相应时间
 *
 * @Author : Williams
 * Date : 2023/12/12 17:29
 */
public class MinimumRespTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumRespTimeSelector(serviceList);
    }

    private static class MinimumRespTimeSelector implements Selector {

        List<InetSocketAddress> serviceList;

        public MinimumRespTimeSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
        }

        @Override
        public InetSocketAddress getNode() {
            TreeMap<Long, InetSocketAddress> answerTimeChannelCache = RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE;
            if (answerTimeChannelCache != null && answerTimeChannelCache.size() > 0) {
                return answerTimeChannelCache.get(answerTimeChannelCache.firstKey());
            } else {
                Channel channel = (Channel) RpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
                return (InetSocketAddress) channel.remoteAddress();
            }
        }

        @Override
        public void reBalance() {

        }
    }
}
