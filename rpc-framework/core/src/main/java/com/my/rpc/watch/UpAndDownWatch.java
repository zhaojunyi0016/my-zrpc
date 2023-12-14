package com.my.rpc.watch;

import com.my.rpc.ConsumerNettyBootstrapInitializer;
import com.my.rpc.RpcBootstrap;
import com.my.rpc.discovery.Registry;
import com.my.rpc.loadbalance.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @Author : Williams
 * Date : 2023/12/12 18:49
 */
@Slf4j
public class UpAndDownWatch implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            // 子节点发生了变化
            log.debug("检测到有节点 {} 上下线.....", watchedEvent.getPath());
            Registry registry = RpcBootstrap.getInstance().getRegistry();
            String serviceName = getServiceName(watchedEvent.getPath());
            List<InetSocketAddress> addressList = registry.lookup(serviceName);
            // 处理新增的节点, 因为都是拉取的最新节点
            for (InetSocketAddress address : addressList) {
                // 新增的节点, 会在 address 中, 不在CHANNEL_CACHE中
                if (!RpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    try {
                        Channel channel = ConsumerNettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                        // 刷新到缓存中
                        log.warn("服务节点发生变化.... 新加入节点[{}]...", address.getAddress());
                        RpcBootstrap.CHANNEL_CACHE.put(address, channel);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }


            // 处理下线的节点, 可能在缓存里面
            for (Map.Entry<InetSocketAddress, Channel> entry : RpcBootstrap.CHANNEL_CACHE.entrySet()) {
                // 不在 address 里面, 说明已经下线了
                if (!addressList.contains(entry.getKey())) {
                    log.warn("服务节点发生变化.... 节点[{}]已下线...", entry.getValue().remoteAddress());
                    RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // 获取负债均衡器, 进行 loadbalance
            LoadBalancer loadBalance = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalance.reBalance(serviceName, addressList);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
