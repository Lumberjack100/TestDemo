package com.shmedo.lib.device.base.iot_cmd;

import com.shmedo.lib.device.base.Validater;
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType;

/**
 * 命令类，把命令头和命令码和参数组装成相应的命令
 */
public class IOTCommand<T extends Validater> {
    public static final String COMMAND_HEADER = "$cmd=";

    private IOTCommandType commandType;

    private T parameters;

    private String apiKey;

    public IOTCommand(IOTCommandType commandType) {
        this.commandType = commandType;
    }

    public IOTCommand(IOTCommandType commandType, T parameters) {
        this.commandType = commandType;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
//        if (parameters != null)
//            parameters.validate();

        if (parameters == null)
            return COMMAND_HEADER + commandType.toString();
        else
            return COMMAND_HEADER + commandType.toString() + "&"
                    + parameters.toString();
    }
}
