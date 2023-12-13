package com.my.rpc.transport.message;

/**
 * @Author : Williams
 * Date : 2023/12/8 14:38
 */
public class MessageFormatConstant {

    /**
     * 魔数值
     */
    public final static byte[] MAGIC = "fast".getBytes();


    /**
     * 版本号
     */
    public final static byte VERSION = 1;


    /**
     * 版本号长度
     */
    public final static int VERSION_LENGTH = 1;


    /**
     * header 信息长度
     */
    public final static short HEADER_LENGTH = (byte) 4 + 1 + 2 + 4 + 1 + 1 + 1 + 8 + 8;


    /**
     * header length 的长度
     */
    public final static int HEADER_FIELD_LENGTH = 2;


    /**
     * 最大帧长度  1MB
     */
    public final static int MAX_FRAME_LENGTH = 1024 * 1024;


    /**
     * 总长占用的字节数
     */
    public final static int FULL_FIELD_LENGTH = 4;


}
