package com.my.rpc.exception;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:06
 */
public class DiscoveryException extends RuntimeException {
    private String msg;

    public DiscoveryException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public DiscoveryException(Exception msg) {
        super(msg);
    }

    public DiscoveryException() {
        super();
    }
}
