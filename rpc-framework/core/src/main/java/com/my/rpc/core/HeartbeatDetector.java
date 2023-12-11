package com.my.rpc.core;

import com.my.rpc.RpcBootstrap;
import com.my.rpc.discovery.Registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 心跳检测器
 * @Author : Williams
 * Date : 2023/12/11 17:49
 */
public class HeartbeatDetector {

    public static void testHeartbeat(String ServiceName){
        Registry registry = RpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> lookup = registry.lookup(ServiceName);
    }
}
