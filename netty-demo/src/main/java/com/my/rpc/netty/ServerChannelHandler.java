package com.my.rpc.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 * @Author : Williams
 * Date : 2023/11/23 18:34
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 处理收到的数据,并反馈消息到到客户端
        ByteBuf in = (ByteBuf) msg;
        System.out.println("收到客户端发过来的消息:" + in.toString(StandardCharsets.UTF_8));

        //写入并发送信息到远端（客户端）
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("你好,我是服务端,我己经收到你发送的消息", CharsetUtil.UTF_8));
    }
}
