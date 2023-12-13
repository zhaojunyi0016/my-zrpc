package com.my.rpc.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡抽象接口
 *
 * @Author : Williams
 * Date : 2023/12/11 16:04
 */
public interface LoadBalance {


    /**
     * 根据服务名称, 返回一个可用的服务
     *
     * @param serviceName 服务名称
     * @return 可用的具体节点
     */
    InetSocketAddress selectAddress(String serviceName);


    /**
     * 当感知到节点发生变化, 需要进行重新负载均衡
     *
     * @param serviceName
     * @param addressList
     */
    void reBalance(String serviceName, List<InetSocketAddress> addressList);
}
