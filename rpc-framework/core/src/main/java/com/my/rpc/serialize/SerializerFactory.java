package com.my.rpc.serialize;

import com.my.rpc.exception.SerializerException;
import com.my.rpc.serialize.impl.HessianSerializer;
import com.my.rpc.serialize.impl.JdkSerializer;
import com.my.rpc.serialize.impl.JsonSerializer;
import com.my.rpc.serialize.impl.ProtocolBufferSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取序列化器的简单静态工厂, 通过配置获取具体的序列化器
 *
 * @Author : Williams
 * Date : 2023/12/10 00:52
 */
public class SerializerFactory {

    private final static Map<String, Serializer> serializerMap = new HashMap<>();

    static {
        serializerMap.put("jdk", new JdkSerializer());
        serializerMap.put("json", new JsonSerializer());
        serializerMap.put("hessian", new HessianSerializer());
        serializerMap.put("Protocol buffer", new ProtocolBufferSerializer());
    }

    public static Serializer getSerializer(String serializeMode) {
        if (serializeMode != null) {
            return serializerMap.get(serializeMode);
        }
        throw new SerializerException("未匹配到对应序列化方式");
    }

    /**
     * 将序列化器 添加进工厂
     *
     * @param serializerMode 序列化模式
     * @param serializer     序列化器
     */
    public static void addSerializer(String serializerMode, Serializer serializer) {
        serializerMap.put(serializerMode, serializer);
    }
}
