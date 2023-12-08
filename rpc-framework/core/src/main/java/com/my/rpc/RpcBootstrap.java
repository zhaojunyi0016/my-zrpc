package com.my.rpc;

import com.my.rpc.channelHandler.handler.MethodCallHandler;
import com.my.rpc.channelHandler.handler.RpcMessageDeEncoder;
import com.my.rpc.discovery.Registry;
import com.my.rpc.discovery.RegistryConfig;
import com.my.rpc.protocol.ProtocolConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : Williams
 * Date : 2023/12/5 15:00
 */
@Slf4j
public class RpcBootstrap {

    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

    // 默认名称
    private String appName = "default";

    // 注册中心
    private RegistryConfig registryConfig;

    // 序列化协议
    private ProtocolConfig protocolConfig;

    // 注册中心
    private Registry registry;

    // 连接的缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();

    // 维护已经发布的服务列表  key -> interface全限定名称
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>();

    // 定义全局的 completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();

    private RpcBootstrap() {
    }

    /**
     * 创建实例 - 单例模式
     *
     * @return RpcBootstrap
     */
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * 定义当前应用的名字
     *
     * @param appName
     * @return this
     */
    public RpcBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registryConfig
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
        this.registry = registryConfig.getRegistryByCode(registryConfig);
        return this;
    }

    /**
     * 配置当前配置服务的序列化协议
     *
     * @param protocolConfig 协议的封装
     * @return this
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        log.debug("当前工程使用了  {}   协议进行序列化", protocolConfig.getProtocolName());
        return this;
    }

    /**
     * 启动netty服务
     */
    public RpcBootstrap start() {
        log.debug("项目启动中....");
        // 1. 创建 bossGroup, 只负责处理请求 IO , 之后会将请求分发到 workGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workGroup = new NioEventLoopGroup(10);
        try {

            // 需要服务器引导程序 ServerBootStrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 配置服务器
            serverBootstrap = serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LoggingHandler());
                            // 解码器
                            socketChannel.pipeline().addLast(new RpcMessageDeEncoder());
                            // 根据请求进行方法调用
                            socketChannel.pipeline().addLast(new MethodCallHandler());
                        }
                    });
            // 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();
            log.debug("项目启动完成...");
            // 接受客户端发送的消息
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bossGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                workGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.warn("项目关闭");
        }
        return this;
    }


    //--------------------------------服务提供方的 api----------------------------------------

    /**
     * 发布服务 - 将匹配的接口, 注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this 当前实例
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        // 使用注册中心 发布对应接口
        registry.publish(service);

        // 1. 当服务调用方, 通过接口, 方法名, 具体的方法参数 发起调用, 提供方怎么知道使用哪个实现
        // (1) new 一个  (2) 通过 spring beanFactory.getBean(Class)   (3) 自己维护映射关系
        SERVICE_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     *
     * @param services 封装的需要发布的服务
     * @return this
     */
    public RpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    //--------------------------------服务提供方的 api-----------------------------------------








    //--------------------------------服务调用方的 api-----------------------------------------

    /**
     * @return
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是香可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        reference.setRegistry(registry);
        return this;
    }


    //--------------------------------服务调用方的 api-----------------------------------------

}
