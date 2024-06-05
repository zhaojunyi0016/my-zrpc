package com.my.rpc;

import com.my.rpc.annotation.RpcApi;
import com.my.rpc.channelHandler.handler.MethodCallHandler;
import com.my.rpc.channelHandler.handler.RpcRequestDeEncoder;
import com.my.rpc.channelHandler.handler.RpcResponseEncoder;
import com.my.rpc.config.Configuration;
import com.my.rpc.discovery.Registry;
import com.my.rpc.discovery.RegistryConfig;
import com.my.rpc.heartbeat.HeartbeatDetector;
import com.my.rpc.hook.RpcShutdownHook;
import com.my.rpc.loadbalance.LoadBalancer;
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @Author : Williams
 * Date : 2023/12/5 15:00
 */
@Slf4j
public class RpcBootstrap {

    // 连接的缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    // 最短响应时间
    public static final TreeMap<Long, InetSocketAddress> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();
    // 维护已经发布的服务列表  key -> interface全限定名称
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>();
    // 定义全局的 completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    // 全局的配置中心
    private final Configuration configuration;
    // 注册中心
    public Registry registry;


    private RpcBootstrap() {
        // 创建配置中心
        configuration = new Configuration();
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
        configuration.setAppName(appName);
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registryConfig
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        this.registry = registryConfig.getRegistryByCode(registryConfig.getRegistryCode());
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registryConfig
     * @return this
     */
    public RpcBootstrap registry() {
        RegistryConfig registryConfig = configuration.getRegistryConfig();
        this.registry = registryConfig.getRegistryByCode(registryConfig.getRegistryCode());
        return this;
    }

    /**
     * 配置负债均衡
     *
     * @param loadBalancer 负债均衡器
     * @return this
     */
    public RpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }


    /**
     * 配置序列化方式
     *
     * @param serializeMode 序列化方式
     * @return
     */
    public RpcBootstrap serialize(String serializeMode) {
        configuration.setSerializeMode(serializeMode);
        if (serializeMode != null) {
            log.debug("配置了序列化方式为 = {}", serializeMode);
        }
        return this;
    }

    /**
     * 配置序列化方式
     */
    public RpcBootstrap serialize() {
        log.debug("配置了默认的序列化方式..");
        return this;
    }

    public RpcBootstrap compress(String compressMode) {
        if (compressMode != null) {
            log.debug("配置了压缩方式为 = {}", compressMode);
        }
        return this;
    }

    public RpcBootstrap compress() {
        log.debug("配置了默认的压缩方式");
        return this;
    }


    /**
     * 启动netty服务
     */
    public RpcBootstrap start() {
        // 注册应用关闭钩子函数
        Runtime.getRuntime().addShutdownHook(new RpcShutdownHook());
        // TODO 查看序列化 负债均衡 压缩 是否为空, 空的话说明没有配置 xml, 没有配置 spi, 没有在启动引导的时候使用代码, 走默认配置实例化
        log.debug("项目启动中...");
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
                            // 打印日志
                            socketChannel.pipeline().addLast(new LoggingHandler());
                            // 解码器解析请求
                            socketChannel.pipeline().addLast(new RpcRequestDeEncoder());
                            // 根据请求进行方法调用
                            socketChannel.pipeline().addLast(new MethodCallHandler());
                            // 将响应结果编码
                            socketChannel.pipeline().addLast(new RpcResponseEncoder());
                        }
                    });
            // 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
            log.debug("项目启动完成..");
            System.out.println("项目启动完成");
            // 接受客户端发送的消息
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bossGroup.shutdownGracefully().sync();
                workGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.warn("项目关闭.");
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

    /**
     * 扫描包下的类, 发布服务
     *
     * @param packageName
     * @return
     */
    public RpcBootstrap scan(String packageName) {
        // 1. 通过包名, 获取其下所有的类的全限定类名
        List<String> classNames = getAllClassNames(packageName);

        // 2. 通过反射获取具体实现接口 , 过滤出只有 @RpcApi 注解的类
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(RpcApi.class) != null)
                .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            // 获取接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            // 循环所有接口, 暴露出去
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                // 3. 发布服务
                publish(serviceConfig);
                log.debug("通过包扫描, 发布服务[{}]成功.....", anInterface.getName());
            }
        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        // 1. 通过 packageName 获取绝对路径
        String basePath = packageName.replace(".", "/");
        System.out.println("basePath = " + basePath);
        URL url = ClassLoader.getSystemClassLoader()
                .getResource(basePath);
        if (url == null) {
            log.error("Resource not found...");
            throw new RuntimeException("Resource not found....");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath, classNames, basePath);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        // 判断是否文件夹
        if (file.isDirectory()) {
            File[] chilFiles = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (chilFiles == null) {
                return classNames;
            }
            for (File chilFile : chilFiles) {
                if (chilFile.isDirectory()) {
                    // 递归调用
                    recursionFile(chilFile.getAbsolutePath(), classNames, basePath);
                } else {
                    // 文件 -> 累的全限定类名
                    classNames.add(getClassNameByAbsolutePath(chilFile.getAbsolutePath(), basePath));
                }
            }
        } else {
            // 文件 -> 累的全限定类名
            classNames.add(getClassNameByAbsolutePath(absolutePath, basePath));
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileNams = absolutePath.substring(absolutePath.indexOf(basePath)).replace("/", ".");
        return fileNams.substring(0, fileNams.indexOf(".class"));
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

        // 开启这个服务的心跳检测
        System.out.println("开始心跳检测....");
        HeartbeatDetector.detectorHeartbeat(reference.getInterfaceRef().getName());
        return this;
    }


//--------------------------------服务调用方的 api-----------------------------------------

    /**
     * 获取全局配置
     *
     * @return Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }
}
