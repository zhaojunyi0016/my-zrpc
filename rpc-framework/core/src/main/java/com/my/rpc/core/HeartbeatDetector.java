package com.my.rpc.core;

import com.my.rpc.ConsumerNettyBootstrapInitializer;
import com.my.rpc.RpcBootstrap;
import com.my.rpc.discovery.Registry;
import com.my.rpc.enums.CompressEnum;
import com.my.rpc.enums.RequestEnum;
import com.my.rpc.enums.SerializeEnum;
import com.my.rpc.transport.message.RpcRequest;
import com.my.rpc.utils.SnowflakeIdGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳检测器
 * 感知服务哪些是正常的, 哪些是不正常的
 * 不正常的下线-->
 *
 * @Author : Williams
 * Date : 2023/12/11 17:49
 */
@Slf4j
public class HeartbeatDetector {

    public static void detectorHeartbeat(String ServiceName) {
        Registry registry = RpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addressList = registry.lookup(ServiceName);
        for (InetSocketAddress address : addressList) {
            try {
                // 拿到连接
                if (RpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = ConsumerNettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    RpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Thread heartbeat = new Thread(() -> {
            new Timer().scheduleAtFixedRate(new HearbeatTimerTask(), 0, 2000);
        }, "heartbeat-thread");
        heartbeat.setDaemon(true);
        heartbeat.start();
    }

    private static class HearbeatTimerTask extends TimerTask {

        @Override
        public void run() {
            RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            // 遍历所有 channel
            Map<InetSocketAddress, Channel> channelCache = RpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()) {
                int tryTimes = 3;
                while (tryTimes > 0) {
                    Channel channel = entry.getValue();
                    InetSocketAddress address = entry.getKey();


                    // 构建一个心跳请求
                    long start = System.currentTimeMillis();
                    SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 2);
                    long requestId = snowflakeIdGenerator.getId();
                    RpcRequest heartbeat = RpcRequest.builder()
                            .requestId(requestId)
                            .requestType(RequestEnum.HEART_BEAT.getId())
                            .compressType(CompressEnum.getCodeByDesc(RpcBootstrap.COMPRESS_MODE))
                            .timestamp(start)
                            .serializeType(SerializeEnum.getCodeByDesc(RpcBootstrap.SERIALIZE_MODE)).build();

                    CompletableFuture<Object> heartbeatFuture = new CompletableFuture<>();
                    RpcBootstrap.PENDING_REQUEST.put(requestId, heartbeatFuture);

                    channel.writeAndFlush(heartbeat).addListener((ChannelFutureListener) promise -> {
                        // 发送出去经过 pipeline 加工处理
                        // 捕获 发数据的结果是否异常
                        if (!promise.isSuccess()) {
                            heartbeatFuture.completeExceptionally(promise.cause());
                        }
                    });

                    Long endTime = 0L;
                    try {
                        Object o = heartbeatFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        log.debug("和服务器 [{}] 连接响应时间超过配置的1秒,正在进行第{}次重试", entry.getKey(), 3 - tryTimes);
                        tryTimes--;
                        if (tryTimes == 0) {
                            // 移除服务列表
                            // 节点不用删除, 是临时节点, 只需要删除缓存即可
                            RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        try {
                            Thread.sleep(10 * (3 - tryTimes));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    Long time = endTime - start;
                    // 维护一个响应时间的 channel
                    RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, address);
                    log.debug("consumer 和服务器{}的响应时间是{}", entry.getKey(), time);
                    // 正常结束 直接 break
                    break;
                }
            }

            log.debug("最小相应时间 map ={}", RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE);
        }
    }
}
