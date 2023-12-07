package com.my.rpc.exception;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:06
 */
public class NetException extends RuntimeException {
    private String msg;

    public NetException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public NetException() {
        super();
    }
}
