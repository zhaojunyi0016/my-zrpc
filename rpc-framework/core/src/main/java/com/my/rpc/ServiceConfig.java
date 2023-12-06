package com.my.rpc;

import java.util.Objects;

/**
 * @Author : Williams
 * Date : 2023/12/5 15:49
 */
public class ServiceConfig<T> {

    /**
     * class
     */
    private Class<T> interfaceProvide;

    /**
     * 具体实现
     */
    private Object ref;


    public Class<T> getInterface() {
        return interfaceProvide;
    }

    public void setInterface(Class<T> interfaceProvide) {
        this.interfaceProvide = interfaceProvide;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
