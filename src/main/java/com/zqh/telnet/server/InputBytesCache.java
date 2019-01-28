package com.zqh.telnet.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @discription:
 * @date: 2019/01/27 下午5:13
 */
public class InputBytesCache {

    private byte[] cache = new byte[128];
    private AtomicInteger index = new AtomicInteger(0);
    private AtomicInteger cursor = new AtomicInteger(0);

    public String bytesToString() {
        return new String(cache, 0, indexValue());
    }

    public void reset() {
        cache = new byte[128];
        cursor.set(0);
        index.set(0);
    }

    public void insertByte(byte b) {
        insertByteAndGetEchoBytes(b);
    }

    public byte[] insertByteAndGetEchoBytes(byte b) {
        byte[] echoBytes = null;
        if (cache.length <= index.get()) { // 扩容
            byte[] newCache = new byte[cache.length * 2];
            System.arraycopy(cache, 0, newCache, 0, cache.length);
            cache = newCache;
        }
        if (cursor.get() == index.get()) { // 结尾插入
            cache[index.get()] = b;
        } else { // 中间插入
            System.arraycopy(cache, cursor.get(), cache, cursor.get() + 1, getIndexSubtractCursor());
            echoBytes = getEchoBytes();
            cache[cursor.get()] = b;
        }
        index.getAndIncrement();
        cursor.getAndIncrement();
        return echoBytes;
    }

    public void deleteCursorByte() {
        System.arraycopy(cache, cursor.get(), cache, cursor.get() - 1, index.get() - cursor.get());
        cache[index.get()] = 0;
        cursor.getAndDecrement();
        index.getAndDecrement();
    }

    public byte[] getEchoBytes() {
        byte[] echoBytes = new byte[index.get() - cursor.get()];
        System.arraycopy(cache, cursor.get(), echoBytes, 0, index.get() - cursor.get());
        return echoBytes;
    }

    public void deleteIndexByte() {
        if (!isIndexStart()) {
            cache[index.getAndDecrement()] = 0;
            if (!isCursorStart()) {
                cursor.getAndDecrement();
            }
        } else {
            index.set(0);
            cursor.set(0);
            cache[0] = 0;
        }
    }

    public int indexValue() {
        return index.get();
    }

    public int getIndexSubtractCursor() {
        return index.get() - cursor.get();
    }

    public boolean isIndexStart() {
        return index.get() <= 0;
    }

    public boolean indexEqualsCursor() {
        return index.get() == cursor.get();
    }

    public boolean indexBigThanCursor() {
        return index.get() > cursor.get();
    }

    public void cursorRightMove() {
        if (indexBigThanCursor()) {
            cursor.getAndIncrement();
        }
    }

    public void cursorLeftMove() {
        if (!isCursorStart()) {
            cursor.getAndDecrement();
        }
    }

    public boolean isCursorStart() {
        return cursor.get() <= 0;
    }

    public int cursorValue() {
        return cursor.get();
    }

    public void incCursor() {
        cursor.getAndIncrement();
    }

    public void decCursor() {
        cursor.getAndDecrement();
    }

    public byte[] getCache() {
        return cache;
    }

    public void setCache(byte[] cache) {
        this.cache = cache;
    }

}
