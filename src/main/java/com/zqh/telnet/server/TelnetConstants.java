package com.zqh.telnet.server;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @discription:
 * @date: 2019/01/27 上午10:54
 */
public interface TelnetConstants {

    /**
     * command
     * ===================================================================================
     * 发送者 			| 接收者 			| 说明										 *
     * -----------------------------------------------------------------------------------
     * WILL 			| DO 				| 发送者想激活某选项，接受者接收该选项请求 		 *
     * -----------------------------------------------------------------------------------
     * WILL 			| DONT 				| 发送者想激活某选项，接受者拒绝该选项请求 		 *
     * -----------------------------------------------------------------------------------
     * DO 				| WILL 				| 发送者希望接收者激活某选项，接受者接受该请求 	 *
     * -----------------------------------------------------------------------------------
     * DO 				| DONT 				| 发送者希望接收6者激活某选项，接受者拒绝该请求 	 *
     * -----------------------------------------------------------------------------------
     * WONT 			| DONT 				| 发送者希望使某选项无效，接受者必须接受该请求 	 *
     * -----------------------------------------------------------------------------------
     * DONT 			| WONT 				| 发送者希望对方使某选项无效，接受者必须接受该请求 *
     * ===================================================================================
     */
    byte IAC = -1; // (0xFF) 255  选项协商的第一个字节
    byte DONT = -2; // (0xFE) 254  接受方回应WONT
    byte DO = -3; // (0xFD) 253  接收方同意（发送方想让接收方激活选项）
    byte WONT = -4; // (0xFC) 252  接收方不同意
    byte WILL = -5; // (0xFB) 251  发送方激活选项(接收方同意激活选项)
    byte SB = -6; // (0xFA) 250  子选项开始
    byte GA = -7; // (0xF9) 249  继续
    byte EL = -8; // (0xF8) 48  擦除一行
    byte EC = -9; // (0xF7) 247  终止符
    byte AYT = -10; // (0xF6) 246  请求应答
    byte AO = -11; // (0xF5) 245  终止输出
    byte IP = -12; // (0xF4) 244  终止进程
    byte BRK = -13; // (0xF3) 243  终止符（break）
    byte DM = -14; // (0xF2) 242  数据标记
    byte NOP = -15; // (0xF1) 241  空操作
    byte SE = -16; // (0xF0) 240  子选项结束
    byte EOR = -17; // (0xEF) 239  记录结束符
    byte ABORT = -18; // (0xEE) 238  中止进程
    byte SUSP = -19; // (0xED) 237  挂起当前进程
    byte EOF = -20; // (0xEC) 236  文件结束符

    /**
     * option
     */
    byte ECHO = 1; // (0x01) 回显(echo)
    byte SUPPRESS = 3; // (0x03) 抑制继续进行(传送一次一个字符方式可以选择这个选项)
    byte TERMINAL_TYPE = 24; // (0x18) 终端类型
    byte WINDOW_SIZE = 31; // (0x1F) 窗口大小
    byte TERMINAL_SPEED = 32; // (0x20) 终端速率
    byte REMOTE_FLOW_CONTROL = 33; // (0x21) 远程流量控制
    byte LINE_MODE = 34; // (0x22) 行方式
    byte OLD_ENVIRONMENT_VARIABLES = 36; // (0x24) 环境变量
    byte NEW_ENVIRONMENT_VARIABLES = 39; // (0x27) 环境变量

    byte[] UP_KEY = new byte[]{27, 91, 65};
    byte[] DOWN_KEY = new byte[]{27, 91, 66};
    byte[] RIGHT_KEY = new byte[]{27, 91, 67};
    byte[] LEFT_KEY = new byte[]{27, 91, 68};


    enum TelnetCommand {

        IAC(TelnetConstants.IAC) {
            @Override
            public void doCommand(TelnetCommandHolder commandHolder, byte b) {
                commandHolder.setOption(null); // 每个新的指令开始，清除之前的option
                TelnetCommand command = getCommand(b);
                if (SE == command) { // 自选项结束，清除指令
                    command = null;
                }
                commandHolder.setCommand(command);
            }
        },
        DO(TelnetConstants.DO) {
            @Override
            public void doCommand(TelnetCommandHolder commandHolder, byte b) {
                doOption(commandHolder, b);
                commandHolder.setCommand(null);
            }
        },
        WILL(TelnetConstants.WILL) {
            @Override
            public void doCommand(TelnetCommandHolder commandHolder, byte b) {
                doOption(commandHolder, b);
                commandHolder.setCommand(null);
            }
        },
        SB(TelnetConstants.SB) {
            @Override
            public void doCommand(TelnetCommandHolder commandHolder, byte b) {
                doOption(commandHolder, b);
            }
        },
        SE(TelnetConstants.SE) {
            @Override
            public void doCommand(TelnetCommandHolder commandHolder, byte b) {
                super.doCommand(commandHolder, b);
            }
        };

        public static TelnetCommand getCommand(byte code) {
            for (TelnetCommand command : TelnetCommand.values()) {
                if (command.code == code) {
                    return command;
                }
            }
            return null;
        }

        public void doCommand(TelnetCommandHolder commandHolder, byte b) {

        }

        public byte getCode() {
            return this.code;
        }

        private static void doOption(TelnetCommandHolder commandHolder, byte b) {
            TelnetOption option = TelnetOption.getOption(b);
            if (null == commandHolder.getOption() || null != option && SB != commandHolder.getCommand()) {
                commandHolder.setOption(option);
            } else {
                commandHolder.getOption().doOption(commandHolder, b);
            }
        }

        private byte code;

        TelnetCommand(byte code) {
            this.code = code;
        }

    }

    enum TelnetOption {

        ECHO(TelnetConstants.ECHO),
        SUPPRESS(TelnetConstants.SUPPRESS),
        TERMINAL_TYPE(TelnetConstants.TERMINAL_TYPE) {
            @Override
            public void doOption(TelnetCommandHolder commandHolder, byte b) {
                if (ArrayUtils.isEmpty(commandHolder.getData())) {
                    for (byte h : "terminal type: ".getBytes()) {
                        commandHolder.addDataByte(h);
                    }
                }
                if (0 != b) {
                    commandHolder.addDataByte(b);
                }
            }
        },
        WINDOW_SIZE(TelnetConstants.WINDOW_SIZE) {
            @Override
            public void doOption(TelnetCommandHolder commandHolder, byte b) {
                if (ArrayUtils.isEmpty(commandHolder.getResponse())) {
                    for (byte r : new byte[]{TelnetConstants.IAC, TelnetConstants.SB, TelnetConstants.TERMINAL_TYPE, TelnetConstants.ECHO, TelnetConstants.IAC, TelnetConstants.SE}) {
                        commandHolder.addResopnseByte(r);
                    }
                }
                commandHolder.addDataByte(b);
                if (4 == commandHolder.getIndexValue()) {
                    byte[] data = commandHolder.getAndResetData();
                    int width = ((data[0] & 0xff) << 8) + (data[1] & 0xff);
                    int height = ((data[2] & 0xff) << 8) + (data[3] & 0xff);
                    StringBuffer windowSizeInfo = new StringBuffer();
                    windowSizeInfo.append("window size: ")
                            .append(width)
                            .append(" x ")
                            .append(height);
                    for (byte h : windowSizeInfo.toString().getBytes()) {
                        commandHolder.addDataByte(h);
                    }
                }
            }
        };

        public void doOption(TelnetCommandHolder commandHolder, byte b) {
            System.out.println(commandHolder.getCommand().name() + " " + this.name() + " option!");
        }

        public static TelnetOption getOption(byte code) {
            for (TelnetOption option : TelnetOption.values()) {
                if (option.code == code) {
                    return option;
                }
            }
            return null;
        }

        private byte code;

        TelnetOption(byte code) {
            this.code = code;
        }
    }

}
