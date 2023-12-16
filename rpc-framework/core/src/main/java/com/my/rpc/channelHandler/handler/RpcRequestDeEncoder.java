package com.my.rpc.channelHandler.handler;

import com.my.rpc.enums.RequestEnum;
import com.my.rpc.enums.SerializeEnum;
import com.my.rpc.serialize.Serializer;
import com.my.rpc.serialize.SerializerFactory;
import com.my.rpc.transport.message.MessageFormatConstant;
import com.my.rpc.transport.message.RequestPayload;
import com.my.rpc.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求解码器 : 基于长度字段的帧解码器: 入站时 -> 将二进制编码转成报文
 * 4B magic 魔数
 * 1B version 版本
 * 2B header length 头部的长度
 * 4B full length  报文总长度
 * 1B request type 请求的类型
 * 1B serialize type  序列化的类型
 * 1B compress  type 压缩的类型
 * 8B requestId 请求 id
 * 8B timestamp 时间戳
 * Body  通过总报文长度减去其他所有加起来的长度获取
 *
 * @Author : Williams
 * Date : 2023/12/8 14:23
 */
@Slf4j
public class RpcRequestDeEncoder extends LengthFieldBasedFrameDecoder {

    public RpcRequestDeEncoder() {
        // 找到当前报文的总长度, 截取报文, 再进行解析
        super(
                // 最大帧的长度, 超过 maxFrameLength 的会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                0
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Thread.sleep(50);
        // 解析成字节数组
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) decode;
            return decodeFrame(buf);
        }
        return null;
    }


    /**
     * 解析报文
     *
     * @param buf
     * @return
     */
    private Object decodeFrame(ByteBuf buf) {
        // 1. 解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        buf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("魔数值校验失败 请求入参 magic =" + magic + "定义magic =" + MessageFormatConstant.MAGIC);
            }
        }

        // 2. 解析版本号
        byte version = buf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("版本不被支持 请求入参 version =" + version + "定义 version =" + MessageFormatConstant.VERSION);
        }

        // 3. 解析头部长度
        final short headLength = buf.readShort();
        // 4. full length
        final int fullLength = buf.readInt();
        // 5. 请求类型
        final byte requestType = buf.readByte();
        // 6. 序列化类型
        final byte serializeType = buf.readByte();
        // 7. 压缩类型
        final byte compressType = buf.readByte();
        // 8. 请求Id
        final long requestId = buf.readLong();
        // 9. 时间戳
        final long timestamp = buf.readLong();

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(requestType);
        rpcRequest.setSerializeType(serializeType);
        rpcRequest.setCompressType(compressType);
        rpcRequest.setRequestId(requestId);
        rpcRequest.setTimestamp(timestamp);
        // 心跳请求 没有负载
        if (requestType == RequestEnum.HEART_BEAT.getId()) {
            return rpcRequest;
        }
        // 9. 请求负载
        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        buf.readBytes(payload);

        // 解压缩
//        Compressor compressor = CompressorFactory.getCompressor(CompressEnum.getDescByCode(compressType));
//        payload = compressor.decompress(payload);

        // 反序列化
        Serializer serializer = SerializerFactory.getSerializer(SerializeEnum.getDescByCode(serializeType));
        RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
        rpcRequest.setRequestPayload(requestPayload);
        return rpcRequest;
    }
}
