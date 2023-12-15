package com.my.rpc.config;

import com.my.rpc.discovery.RegistryConfig;
import com.my.rpc.loadbalance.LoadBalancer;
import com.my.rpc.loadbalance.impl.RoundRobinLoadBalance;
import com.my.rpc.protection.breaker.Breaker;
import com.my.rpc.protection.retelimiter.ReteLimiter;
import com.my.rpc.utils.SnowflakeIdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局的配置类 , 代码配置->xml 配置->spi 配置-> 默认项
 *
 * @Author : Williams
 * Date : 2023/12/13 15:58
 */
@Slf4j
@Getter
@Setter
public class Configuration {

    // 限流器 -> 对应每个 ip 请求
    public Map<SocketAddress, ReteLimiter> everyIpRateLimiter = new ConcurrentHashMap<>();

    // 熔断器 -> 对应每个 ip 请求
    public Map<SocketAddress, Breaker> everyIpBreaker = new ConcurrentHashMap<>();

    // 端口号
    private int port = 8090;

    // app name
    private String appName = "default";

    // 配置 - 注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper", "127.0.0.1:2181");

    // 配置 -负载均衡策略 -> 默认轮训
    private LoadBalancer loadBalancer = new RoundRobinLoadBalance();

    // 配置 - 序列化方式
    private String serializeMode = "jdk";

    // 配置 - 压缩协议
    private String compressMode = "gzip";

    // id 生成器
    private SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 2);

    public Configuration() {
        // 1. 默认配置值

        // 2. 读 spi
        SpiResolver.loadFromSpi(this);

        // 3. 读 xml
        XmlResolver.loadFromXml(this);

        // 4. 编程配置项, 由 RpcBootstrap配置
    }


}
