package com.zqh.telnet.server.handler;

import java.util.Arrays;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * @discription:
 * @date: 2019/01/24 上午11:11
 */
public class TelnetByteArrayEncoder extends ByteArrayEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        System.out.println("send: " + Arrays.toString(msg));
        super.encode(ctx, msg, out);
    }

}
