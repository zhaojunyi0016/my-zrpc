package com.my.rpc.enums;

public enum ResponseEnum {
    FAIL((byte) 1, "失败"),
    SUCCESS((byte) 2, "成功"),
    HEARTBEAT((byte) 3, "心跳"),
    ;

    private byte code;
    private String desc;

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
