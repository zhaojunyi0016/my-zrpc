package com.my.rpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的内容
 *
 * @Author : Williams
 * Date : 2023/12/8 11:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse {

    /**
     * 请求id
     */
    private long requestId;

    /**
     * 压缩类型
     */
    private byte compressType;

    /**
     * 序列化方式
     */
    private byte serializeType;

    /**
     * 响应码 2: 成功  1: 异常
     */
    private byte code;

    /**
     * 响应的消息体
     */
    private Object body;

}
