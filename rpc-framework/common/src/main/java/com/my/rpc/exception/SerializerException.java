package com.my.rpc.exception;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:06
 */
public class SerializerException extends RuntimeException {
    private String msg;

    public SerializerException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public SerializerException(Exception msg) {
        super(msg);
    }
}
