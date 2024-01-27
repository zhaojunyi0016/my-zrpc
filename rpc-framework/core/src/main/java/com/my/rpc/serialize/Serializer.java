package com.my.rpc.serialize;

/**
 * 序列化器
 *
 * @Author : Williams
 * Date : 2023/12/10 00:29
 */
public interface Serializer {

    /**
     * 序列化
     *
     * @param object 需要被序列化对象
     * @return byte[] 字节数组
     */
    byte[] serialize(Object object);


    /**
     * 反序列化
     *
     * @param bytes 字节数组
     * @param clazz Class 对象
     * @return T 对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
