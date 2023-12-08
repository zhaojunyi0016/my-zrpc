package com.my.rpc.channelHandler.handler;

import com.my.rpc.transport.message.MessageFormatConstant;
import com.my.rpc.transport.message.RequestPayload;
import com.my.rpc.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 编码器: 出站时 -> 将报文转成二进制编码
 * 4B magic 魔数
 * 1B version 版本
 * 2B header length 头部的长度
 * 4B full length  报文总长度
 * 1B request type 请求的类型
 * 1B serialize type  序列化的类型
 * 1B compress  type 压缩的类型
 * 8B requestId 请求 id
 * Body  通过总报文长度减去其他所有加起来的长度获取
 *
 * @Author : Williams
 * Date : 2023/12/8 14:23
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    // 传入的是 rpcRequest 对象,  通过 byteBuf 写出去
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        // 4 个字节 魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 1个字节 版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2个字节 header 信息长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // full length  先空出 4个比特位置
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        // request type
        byteBuf.writeByte(rpcRequest.getRequestType());
        // serialize type
        byteBuf.writeByte(rpcRequest.getSerializeType());
        // compress  type
        byteBuf.writeByte(rpcRequest.getCompressType());
        // 8个字节  请求id
        byteBuf.writeLong(rpcRequest.getRequestId());
        // 写入 body
        byte[] bodyBytes = getBodyBytes(rpcRequest.getRequestPayload());
        byteBuf.writeBytes(bodyBytes);

        // 写指针最后的位置
        int lastIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(7);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        // 写指针归为
        byteBuf.writerIndex(lastIndex);
    }


    /**
     * 将对象转换成字节数组
     *
     * @param requestPayload body 消息体
     * @return byte[]
     */
    private byte[] getBodyBytes(RequestPayload requestPayload) {
        // TODO 针对不同的消息类型, 需要做不同的处理, 心跳的请求, 是没有 payload

        // TODO 通过设计模式, 让我们可以通过配置得到不同的序列化+压缩方式
        // 对象 序列化 -> 二进制
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(requestPayload);

            // TODO 压缩
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化对象时, 出现异常, error ={}", e);
            throw new RuntimeException(e);
        }
    }
}
