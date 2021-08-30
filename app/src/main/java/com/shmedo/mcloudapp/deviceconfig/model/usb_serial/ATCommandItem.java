package com.shmedo.mcloudapp.deviceconfig.model.usb_serial;

import com.shmedo.configlibrary.at.WHBLE102CommandType;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/13 <br/>
 * 描述：     AT 指令实体类
 */
public class ATCommandItem {
    private WHBLE102CommandType commandType;
    private String command;

    public ATCommandItem(WHBLE102CommandType commandType, String command) {
        this.commandType = commandType;
        this.command = command;
    }

    public WHBLE102CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(WHBLE102CommandType commandType) {
        this.commandType = commandType;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
