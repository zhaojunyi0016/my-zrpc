package com.my.rpc.compress;

import com.my.rpc.compress.impl.GzipCompressor;
import com.my.rpc.exception.SerializerException;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取压缩器的简单静态工厂, 通过配置获取具体的压缩器
 *
 * @Author : Williams
 * Date : 2023/12/10 00:52
 */
public class CompressorFactory {

    private final static Map<String, Compressor> compressorMap = new HashMap<>();

    static {
        compressorMap.put("gzip", new GzipCompressor());
    }

    public static Compressor getCompressor(String compressorMode) {
        if (compressorMode != null) {
            return compressorMap.get(compressorMode);
        }
        throw new SerializerException("未匹配到对应压缩方式");
    }
}
