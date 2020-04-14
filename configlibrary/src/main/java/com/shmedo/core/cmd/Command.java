package com.shmedo.core.cmd;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by Liudongdong on 17/12/11.
 * 命令类，把命令头和命令码和参数组装成相应的命令
 */
public class Command<T extends Validater> {
    public static final String COMMAND_HEADER = "##";

    private CommandType commandType;

    private T parameters;

    public Command(CommandType commandType) {
        this.commandType = commandType;
    }

    public Command(CommandType commandType, T parameters) {
        this.commandType = commandType;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        if (parameters != null)
            parameters.validate();
        String paraString = parameters == null ? "" : parameters.toString();
        return COMMAND_HEADER + commandType.toString() + paraString + "\r\n";
    }
}
