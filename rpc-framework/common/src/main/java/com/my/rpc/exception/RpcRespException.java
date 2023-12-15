package com.my.rpc.exception;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:06
 */
public class RpcRespException extends RuntimeException {
    private String msg;
    private byte code;

    public RpcRespException(String msg, byte code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public RpcRespException(Exception msg) {
        super(msg);
    }
}
