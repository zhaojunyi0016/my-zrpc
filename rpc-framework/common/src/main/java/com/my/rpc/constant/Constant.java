package com.my.rpc.constant;

/**
 * @Author : Williams
 * Date : 2023/12/5 17:23
 */
public class Constant {

    // zk默认链接
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    // zk默认链接
    public static final int DEFAULT_ZK_TIMEOUT = 10000;


    // 顶级目录
    public static final String BASE_PATH = "/myrpc-metaData";

    // 服务提供方目录
    public static final String PROVIDE_PATH = BASE_PATH + "/provide";

    // 调用方目录
    public static final String CONSUMER_PATH = BASE_PATH + "/consumer";
}
