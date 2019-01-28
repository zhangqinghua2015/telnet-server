package com.zqh.telnet.server.handler;

import com.zqh.telnet.server.InputBytesCache;
import com.zqh.telnet.server.util.ChannelUtils;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Handles a server-side channel.
 */
@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws InstantiationException, IllegalAccessException {
        boolean echoProm = true;
        String response = "";
        boolean close = false;

        /**中断正在执行的任务*/
        if ("stop_running_command".equals(request)) {
            if (ChannelUtils.inEcho(ctx.channel())) { // 如果正在往客户端输出则不响应输入内容
                AttributeKey<Thread> attributeKey = AttributeKey.valueOf("echoThread");
                Attribute<Thread> attribute = ctx.channel().attr(attributeKey);
                Thread thread = attribute.get();
                thread.interrupt();
            }
            return;
        }

        if ("\r\n".equals(request)) {
            InputBytesCache cache = ChannelUtils.getAttr(ctx.channel(), "inputBytesCache", InputBytesCache.class);
            String commandStr = cache.bytesToString();
            cache.reset();
            if (commandStr.isEmpty()) {
                response = "\r\nPlease type something.\r\n";
            } else if ("bye".equals(commandStr.toLowerCase())) {
                response = "\r\nHave a good day!\r\n";
                close = true;
            } else if ("echo".equals(commandStr.toLowerCase())) {
                echo(ctx);
                return;
            } else {
                response = "\r\nexecuting: " + commandStr + "\r\n";
            }
        } else {
            response = request;
            echoProm = false;
        }
        response += echoProm ? "$ " : "";
        ChannelFuture future = ctx.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void echo(final ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        Thread echoThread = new Thread(() -> {
            try {
                int i = 1;
                while (true) {
                    if (!channel.isActive()) {
                        return;
                    }
                    System.out.println("echo " + i + " times");
                    channel.write("\r\necho " + i++ + " times\r\n");
                    channel.flush();
                    TimeUnit.SECONDS.sleep(2);
                }
            } catch (InterruptedException e) {
                System.out.println("stop echo");
                channel.attr(AttributeKey.valueOf("inEcho")).set(false);
                channel.attr(AttributeKey.valueOf("echoThread")).set(null);
                ctx.writeAndFlush("\r\nstop echo\r\n$ ");
            }

        });
        echoThread.start();
        channel.attr(AttributeKey.valueOf("inEcho")).set(true);
        channel.attr(AttributeKey.valueOf("echoThread")).set(echoThread);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}