package com.my.rpc.enums;

public enum RequestEnum {
    REQUEST((byte) 1, "普通请求"),
    HEART_BEAT((byte) 2, "心跳检测"),
    ;

    private byte id;
    private String desc;

    RequestEnum(byte id, String desc) {
        this.desc = desc;
        this.id = id;
    }

    public String getDesc() {
        return this.desc;
    }

    public byte getId() {
        return this.id;
    }


}
