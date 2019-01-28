package com.zqh.telnet.server.handler;

import com.zqh.telnet.server.InputBytesCache;
import com.zqh.telnet.server.util.ChannelUtils;

import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.bytes.ByteArrayDecoder;

import static com.zqh.telnet.server.TelnetConstants.DOWN_KEY;
import static com.zqh.telnet.server.TelnetConstants.LEFT_KEY;
import static com.zqh.telnet.server.TelnetConstants.RIGHT_KEY;
import static com.zqh.telnet.server.TelnetConstants.UP_KEY;
import static java.util.Objects.*;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

/**
 * @discription:
 * @date: 2019/01/24 上午11:12
 */
public class TelnetByteArrayDecoder extends ByteArrayDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        InputBytesCache cache = ChannelUtils.getAttr(ctx.channel(), "inputBytesCache", InputBytesCache.class);
        byte[] array = new byte[msg.readableBytes()];
        msg.readBytes(array);
        System.out.println("receive: " + Arrays.toString(array));

        if (ChannelUtils.inEcho(ctx.channel()) && 3 != array[0]) {
            return;
        }

        if (deepEquals(array, UP_KEY) || deepEquals(array, DOWN_KEY)) {
            return;
        } else if (deepEquals(array, LEFT_KEY)) {
            if (!cache.isCursorStart()) {
                cache.cursorLeftMove();
                ctx.pipeline().writeAndFlush(array);
            }
            return;
        } else if (deepEquals(array, RIGHT_KEY)) {
            if (cache.indexBigThanCursor()) {
                cache.cursorRightMove();
                ctx.pipeline().writeAndFlush(array);
            }
            return;
        }
        byte[] echoBytes = new byte[0];
        for (byte b : array) {
            /**删除字符*/
            if (127 == array[0]) {
                if (cache.indexEqualsCursor()) { // 在尾端删除
                    if (!cache.isIndexStart()) {
                        ctx.pipeline().writeAndFlush(formatDeleteIndexByteEcho());
                    }
                    cache.deleteIndexByte();
                } else if (!cache.isCursorStart()) { // 在中间删除
                    echoBytes = getEchoBytes(echoBytes, cache);
                    cache.deleteCursorByte();
                    ctx.pipeline().writeAndFlush(formatDeleteCursorByteEcho(echoBytes, cache));
                }
                return;
            }
            /**回车执行*/
            else if (13 == array[0]) {
                out.add("\r\n");
                return;
            }
            /**ctrl+c终止执行*/
            else if (3 == array[0]) {
                out.add("stop_running_command");
                return;
            }
            /**插入字符*/
            else {
                echoBytes = cache.insertByteAndGetEchoBytes(b);
            }
        }
        out.add(formatInsertCursorByteEcho(echoBytes, array));
    }

    private byte[] getEchoBytes(byte[] echoBytes, InputBytesCache cache) {
        if (echoBytes.length == 0) {
            echoBytes = cache.getEchoBytes();
        }
        return echoBytes;
    }

    private String formatInsertCursorByteEcho(byte[] echoBytes, byte[] inputBytes) {
        StringBuffer sb = new StringBuffer();
        StringBuffer backspaceBuffer = new StringBuffer();
        for (byte b : inputBytes) {
            sb.append((char) b);
        }
        if (isNotEmpty(echoBytes)) {
            for (byte b : echoBytes) {
                sb.append((char) b);
                backspaceBuffer.append("\b");
            }
        }
        return sb.append(backspaceBuffer).toString();
    }

    private String formatDeleteCursorByteEcho(byte[] bytes, InputBytesCache cache) {
        StringBuffer echoBuffer = new StringBuffer();
        echoBuffer.append("\b"); // 退格
        for (byte echoByte : bytes) {
            echoBuffer.append((char) echoByte); // 用后面的字符覆盖前面的字符
        }
        echoBuffer.append(" \b"); // 空格覆盖最后一个字符
        for (int i = 0; i < cache.getIndexSubtractCursor(); i++) { // 光标相对字符串结尾位置不变
            echoBuffer.append("\b");
        }
        return echoBuffer.toString();
    }

    private String formatDeleteIndexByteEcho() {
        return "\b \b";
    }

}
