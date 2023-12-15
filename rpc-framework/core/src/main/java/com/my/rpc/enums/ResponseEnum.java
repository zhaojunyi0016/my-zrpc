package com.my.rpc.enums;

/**
 * 购应码需要做统一的处理
 * 成功码20（方法成功调用）
 * 21（心秘成功反回）
 * 负载码 31（服务器负载过高，被限流）
 * 错误码（客户端错误）44
 * 错误码（服务端错误) 50（请求的方法不存在）
 */
public enum ResponseEnum {
    SUCCESS((byte) 20, "成功"),
    HEARTBEAT((byte) 21, "心跳"),
    RATE_LIMIT((byte) 31, "服务被限流"),
    RESOURCE_NOT_FOUND((byte) 45, "请求的资源不存在"),
    FAIL((byte) 50, "调用方法发生异常"),
    ;

    private final byte code;
    private final String desc;

    ResponseEnum(byte code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return this.desc;
    }

    public byte getCode() {
        return this.code;
    }


}
