package com.my.rpc.serialize.impl;

import com.my.rpc.exception.SerializerException;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;

/**
 * JDK 序列化
 *
 * @Author : Williams
 * Date : 2023/12/10 00:33
 */
@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        // 对象 序列化 -> 二进制
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
        ) {
            oos.writeObject(object);
            log.debug("对象完成了 JDK 序列化....");
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (IOException e) {
            log.error("使用 JDK 序列化对象时, 出现异常 error ={}", e);
            throw new SerializerException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            Object object = ois.readObject();
            log.debug("对象完成了 JDK 反序列化.......");
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("使用 JDK 反序列化对象时, 出现异常 error ={}", e);
            throw new SerializerException(e);
        }
    }


    public static void main(String[] args) {
        Serializer serializer = new JdkSerializer();
        RequestPayload requestPayload = new RequestPayload();
        requestPayload.setInterfaceName("xxx");
        requestPayload.setMethodName("xxx");

        // 加了这个不行 ,  不认 java.lang.String ,  想处理需要处理成字符串
        requestPayload.setReturnType(String.class);

        byte[] serialize = serializer.serialize(requestPayload);
        System.out.println(Arrays.toString(serialize));

        RequestPayload deserialize = serializer.deserialize(serialize, RequestPayload.class);
        System.out.println(deserialize);
    }
}
