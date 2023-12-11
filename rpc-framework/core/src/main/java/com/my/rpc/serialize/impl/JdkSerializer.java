package com.my.rpc.serialize.impl;

import com.my.rpc.exception.SerializerException;
import com.my.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * JDK 序列化
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
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("使用 JDK 序列化对象时, 出现异常 error ={}", e);
            throw new SerializerException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            Object object = ois.readObject();
            log.debug("对象完成了 JDK 反序列化....");
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("使用 JDK 反序列化对象时, 出现异常 error ={}", e);
            throw new SerializerException(e);
        }
    }
}
