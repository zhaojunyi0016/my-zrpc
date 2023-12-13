package com.my.rpc.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * JSON 序列化
 *
 * @Author : Williams
 * Date : 2023/12/10 00:33
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        // 对象 序列化 -> 二进制
        byte[] bytes = JSON.toJSONBytes(object);
        log.debug("对象使用 fastjson 序列化完成...");
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }
        T t = JSON.parseObject(bytes, clazz);
        log.debug("对象使用 fastjson 反序列化完成...");
        return t;
    }

    public static void main(String[] args) {
        Serializer serializer = new JsonSerializer();
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
