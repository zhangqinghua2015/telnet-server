package com.zqh.telnet.server.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @discription:
 * @date: 2019/01/28 下午5:09
 */
public class ChannelUtils {

    public static <T> T getAttr(Channel channel, String attrName, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        if (channel.hasAttr(AttributeKey.valueOf(attrName))) {
            T attr = (T) channel.attr(AttributeKey.valueOf(attrName)).get();
            return attr;
        } else {
            T attr = clazz.newInstance();
            channel.attr(AttributeKey.valueOf(attrName)).set(attr);
            return attr;
        }
    }

    public static boolean inEcho(Channel channel) {
        if (channel.hasAttr(AttributeKey.valueOf("inEcho")) &&
                (Boolean) channel.attr(AttributeKey.valueOf("inEcho")).get()) {
            return true;
        }
        return false;
    }

}
