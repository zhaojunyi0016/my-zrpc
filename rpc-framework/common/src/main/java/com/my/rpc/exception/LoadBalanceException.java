package com.my.rpc.exception;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:06
 */
public class LoadBalanceException extends RuntimeException {
    private String msg;

    public LoadBalanceException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public LoadBalanceException(Exception msg) {
        super(msg);
    }

}
