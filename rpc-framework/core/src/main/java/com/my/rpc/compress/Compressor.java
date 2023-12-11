package com.my.rpc.compress;

/**
 * 压缩器
 *
 * @Author : Williams
 * Date : 2023/12/10 00:29
 */
public interface Compressor {

    /**
     * 压缩
     *
     * @param target 待压缩的字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] target);


    /**
     * 解压缩
     *
     * @param target 待解压缩的字节数组
     * @return 解压缩后的字节数组
     */
    byte[] decompress(byte[] target);

}
