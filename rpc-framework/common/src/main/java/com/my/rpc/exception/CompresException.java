package com.my.rpc.exception;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:06
 */
public class CompresException extends RuntimeException {
    private String msg;

    public CompresException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public CompresException() {
        super();
    }

    public CompresException(Exception msg) {
        super(msg);
    }
}
