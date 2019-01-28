package com.zqh.telnet.server;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.zqh.telnet.server.TelnetConstants.*;
import static com.zqh.telnet.server.TelnetConstants.IAC;

/**
 * @discription:
 * @date: 2019/01/28 上午11:18
 */
public class TelnetCommandHolder {

    private TelnetCommand command;
    private TelnetOption option;
    private byte[] data;
    private AtomicInteger index = new AtomicInteger(0);
    private byte[] response;
    private AtomicInteger responseIndex = new AtomicInteger(0);

    public void doCommand(byte[] commandByte) {
        for (int i=0; i<commandByte.length; i++) {
            if (IAC == commandByte[i]) {
                /**如果当前指令为SB且下一个字节不为SE指令，则当前字节是值为-1的数据*/
                if (command != TelnetCommand.SB || (command == TelnetCommand.SB && commandByte[i+1] == TelnetCommand.SE.getCode())) {
                    command = TelnetCommand.IAC;
                    continue;
                }
            }
            command.doCommand(this, commandByte[i]);
        }
    }

    public void echoData() {
        if (index.get() > 0) {
            System.out.println(new String(data, 0, index.get()));
            getAndResetData();
        }
    }

    public void addDataByte(byte b) {
        data = addByte(data, index, b);
    }

    public void addResopnseByte(byte b) {
        response = addByte(response, responseIndex, b);
    }

    public byte[] addByte(byte[] bytes, AtomicInteger index, byte b) {
        if (null == bytes) {
            bytes = new byte[128];
        }
        if (index.get() > bytes.length) {
            byte[] newData = new byte[bytes.length * 2];
            System.arraycopy(bytes, 0, newData, 0, bytes.length);
            bytes = newData;
        }
        bytes[index.getAndIncrement()] = b;
        return bytes;
    }

    public int getIndexValue() {
        return index.get();
    }

    public byte[] getAndResetData() {
        byte[] result = Arrays.copyOf(data, index.get());
        data = null;
        index.set(0);
        return result;
    }

    public byte[] getAndResetResponse() {
        byte[] result = Arrays.copyOf(response, responseIndex.get());
        response = null;
        responseIndex.set(0);
        return result;
    }

    public TelnetCommand getCommand() {
        return command;
    }

    public void setCommand(TelnetCommand command) {
        this.command = command;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public TelnetOption getOption() {
        return option;
    }

    public void setOption(TelnetOption option) {
        this.option = option;
    }
}
