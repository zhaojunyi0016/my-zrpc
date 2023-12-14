package com.my.rpc.exception;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:06
 */
public class RpcCallException extends RuntimeException {
    private String msg;

    public RpcCallException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public RpcCallException(Exception msg) {
        super(msg);
    }
}
