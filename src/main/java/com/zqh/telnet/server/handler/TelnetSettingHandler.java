package com.zqh.telnet.server.handler;

import com.zqh.telnet.server.TelnetCommandHolder;

import org.apache.commons.lang3.ArrayUtils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import static com.zqh.telnet.server.TelnetConstants.*;

/**
 * @discription:
 * @date: 2019/01/24 上午11:09
 */
public class TelnetSettingHandler extends ChannelInboundHandlerAdapter {

    /*private static final byte[] read0 = new byte[]{-1, -3, 1, -1, -3, 3, -1, -5, 31, -1, -6, 31, 0, -89, 0, 50, -1, -16, -1, -5, 24};
    private static final byte[] read0 = new byte[]{-1, -3, 1, -1, -3, 3, -1, -5, 31, -1, -6, 31, 0, -89, 0, 49, -1, -16, -1, -5, 24};
    private static final byte[] read0 = new byte[]{-1, -3, 1, -1, -3, 3, -1, -5, 31, -1, -6, 31, 0, -93, 0, 51, -1, -16, -1, -5, 24};
    private static final byte[] read1 = new byte[]{-1, -6, 24, 0, 88, 84, 69, 82, 77, 45, 50, 53, 54, 67, 79, 76, 79, 82, -1, -16};*/
    private TelnetCommandHolder commandHolder = new TelnetCommandHolder();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.pipeline().writeAndFlush("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\nIt is " + new Date() + " now.\r\n$ ");
        ctx.pipeline().writeAndFlush(new byte[]{IAC, WILL, ECHO}).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });
        ctx.pipeline().writeAndFlush(new byte[]{IAC, WILL, SUPPRESS}).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });
        ctx.pipeline().writeAndFlush(new byte[]{IAC, DO, WINDOW_SIZE}).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });
        ctx.pipeline().writeAndFlush(new byte[]{IAC, DO, TERMINAL_TYPE}).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf in = (ByteBuf) msg;
            byte[] array = new byte[in.readableBytes()];
            in.getBytes(0, array);
            if (IAC == array[0]) {
                System.out.println("terminal setting handler receive: " + Arrays.toString(array));
                commandHolder.doCommand(array);
                commandHolder.echoData();
                if (!ArrayUtils.isEmpty(commandHolder.getResponse())) {
                    ctx.pipeline().writeAndFlush(commandHolder.getAndResetResponse()).addListener(future -> {
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }
                    });
                }
                return;
            }
        }
        super.channelRead(ctx, msg);
    }
}
