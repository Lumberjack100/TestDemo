package com.shmedo.configlibrary.iot.cmd;

import com.shmedo.configlibrary.ble.interfaces.Validater;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;

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

        String paraString = parameters == null ? "" : parameters.toString();

        return COMMAND_HEADER + commandType.toString() + "&"
                + paraString;
//                + "&apikey=" + apiKey
//                + "&msgid=" + UUID.randomUUID().toString();
//                + "&&";
    }
}
