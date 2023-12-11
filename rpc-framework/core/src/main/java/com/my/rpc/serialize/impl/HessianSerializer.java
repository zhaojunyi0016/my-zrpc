package com.my.rpc.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.my.rpc.exception.SerializerException;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Hessian序列化是一种支持动态类型、跨语言、基于对象传输的网络协议，Java对象序列化的二进制流可以被其他语言（如，c++，python）。
 * 特性如下：
 * 1. 自描述序列化类型。不依赖外部描述文件或者接口定义，用一个字节表示常用的基础类型，极大缩短二进制流。
 * 2. 语言无关，支持脚本语言
 * 3. 协议简单，比ava原生序列化高效
 * 4. 相比hessian1，hessian2中增加了压缩编码，其序列化二进制流大小是Java序列化的50%，序列化耗时是Java序列化的30%，反序列化耗时是 Java序列化的20%。
 *
 * @Author : Williams
 * Date : 2023/12/10 00:33
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        // 对象 序列化 -> 二进制
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            Hessian2Output ho = new Hessian2Output(baos);
            ho.writeObject(object);
            ho.flush();
            log.debug("对象完成了 hessian 序列化....");
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("使用 hessian 序列化对象时, 出现异常 error ={}", e);
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
        ) {
            Hessian2Input hi = new Hessian2Input(bis);
            Object object = hi.readObject();
            log.debug("对象完成了 hessian 反序列化....");
            return (T) object;
        } catch (IOException e) {
            log.error("使用 hessian 反序列化对象时, 出现异常 error ={}", e);
            throw new SerializerException(e);
        }
    }


    public static void main(String[] args) {
        Serializer serializer = new HessianSerializer();
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
