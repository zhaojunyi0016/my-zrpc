package com.my.rpc.loadbalance;

import com.my.rpc.RpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负载均衡模板方法类
 *
 * @Author : Williams
 * Date : 2023/12/11 17:05
 */
public abstract class AbstractLoadBalancer implements LoadBalance {

    // 一个接口 匹配一个 selector
    private Map<String, Selector> selectorCache = new ConcurrentHashMap<>();

    @Override
    public InetSocketAddress selectAddress(String serviceName) {
        Selector selector = selectorCache.get(serviceName);
        if (selector == null) {
            List<InetSocketAddress> serviceList = RpcBootstrap.getInstance().getRegistry().lookup(serviceName);
            selector = getSelector(serviceList);
            selectorCache.put(serviceName, selector);
        }
        return selector.getNode();
    }


    /**
     * 由子类自己拓展
     *
     * @param serviceList 服务列表
     * @return 具体算法的选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);


}
