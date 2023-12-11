package com.my.rpc.enums;

public enum SerializeEnum {
    JDK((byte) 1, "jdk"),
    JSON((byte) 2, "json"),
    HESSIAN((byte) 3, "hessian"),
    ;

    private byte code;
    private String desc;

    SerializeEnum(byte code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return this.desc;
    }

    public byte getCode() {
        return this.code;
    }


    public static byte getCodeByDesc(String desc) {
        if (desc != null) {
            for (SerializeEnum utim : SerializeEnum.values()) {
                if (utim.getDesc().equals(desc)) {
                    return utim.code;
                }
            }
        }
        return Byte.parseByte(null);
    }


    public static String getDescByCode(byte code) {
        for (SerializeEnum utim : SerializeEnum.values()) {
            if (utim.getCode() == code) {
                return utim.desc;
            }
        }
        return null;
    }
}
