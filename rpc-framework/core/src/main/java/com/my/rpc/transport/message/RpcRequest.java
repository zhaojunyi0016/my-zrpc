package com.my.rpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务调用方发起的请求内容
 *
 * @Author : Williams
 * Date : 2023/12/8 11:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest {

    /**
     * 请求id
     */
    private long requestId;

    /**
     * 请求类型
     */
    private byte requestType;

    /**
     * 压缩类型
     */
    private byte compressType;

    /**
     * 序列化方式
     */
    private byte serializeType;

    /**
     * 消息体
     */
    private RequestPayload requestPayload;

    /**
     * 时间戳
     */
    private long timestamp;
}
