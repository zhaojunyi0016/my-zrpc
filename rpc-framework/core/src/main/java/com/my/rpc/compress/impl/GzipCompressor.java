package com.my.rpc.compress.impl;

import com.my.rpc.compress.Compressor;
import com.my.rpc.exception.CompresException;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.serialize.impl.JdkSerializer;
import com.my.rpc.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip 压缩器
 *
 * @Author : Williams
 * Date : 2023/12/11 14:26
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] target) {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream(target.length);
                GZIPOutputStream gzip = new GZIPOutputStream(bos);
        ) {
            gzip.write(target);
            byte[] bytes = bos.toByteArray();
            log.debug("使用 gzip 压缩完成....由原来的长度{}, 变成了 {}", target.length, bytes.length);
            return bytes;
        } catch (IOException e) {
            log.error("进行 gzip 压缩时, 发生异常 error ={}", e);
            throw new CompresException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] target) {
        // target 是一个通过 gzip 压缩后的字节数组, 怎么解压缩, 帮我生成对应代码
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(target);
                GZIPInputStream gis = new GZIPInputStream(bis);
        ) {
            byte[] data = new byte[target.length * 3];
            int bytesRead;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while(true) {
                int offset = -1;
                try{offset = gis.read(data);}catch(EOFException ex){}
                if(offset!=-1){
                    bos.write(data, 0, offset);
                }else{
                    break;
                }
            }
            byte[] decompressed = bos.toByteArray();
            log.debug("使用 gzip 解压完成....由原来的长度{}, 变成了 {}", target.length, decompressed.length);
            return decompressed;
        } catch (IOException e) {
            log.error("进行 gzip 反压缩时, 发生异常 error ={}", e);
            throw new CompresException(e);
        }
    }

    public static void main(String[] args) {
        Serializer serializer = new JdkSerializer();
        RequestPayload requestPayload = new RequestPayload();
        requestPayload.setInterfaceName("xxx");
        requestPayload.setMethodName("xxx");
        requestPayload.setReturnType(String.class);
        System.out.println(requestPayload);

        byte[] serialize = serializer.serialize(requestPayload);
        System.out.println(Arrays.toString(serialize));


        GzipCompressor gzipCompressor = new GzipCompressor();
        byte[] compress = gzipCompressor.compress(serialize);

        byte[] decompress = gzipCompressor.decompress(compress);

        RequestPayload deserialize = serializer.deserialize(serialize, RequestPayload.class);
        System.out.println(deserialize);


    }
}
