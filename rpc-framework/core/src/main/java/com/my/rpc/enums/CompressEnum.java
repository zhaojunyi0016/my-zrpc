package com.my.rpc.enums;

public enum CompressEnum {
    GZIP((byte) 1, "gzip"),
    ;

    private byte code;
    private String desc;

    CompressEnum(byte code, String desc) {
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
            for (CompressEnum utim : CompressEnum.values()) {
                if (utim.getDesc().equals(desc)) {
                    return utim.code;
                }
            }
        }
        return Byte.parseByte(null);
    }


    public static String getDescByCode(byte code) {
        for (CompressEnum utim : CompressEnum.values()) {
            if (utim.getCode() == code) {
                return utim.desc;
            }
        }
        return null;
    }
}
