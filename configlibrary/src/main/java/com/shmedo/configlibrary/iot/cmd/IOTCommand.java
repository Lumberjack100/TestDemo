package com.shmedo.configlibrary.iot.cmd;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;

import java.util.UUID;

/**
 * 命令类，把命令头和命令码和参数组装成相应的命令
 */
public class IOTCommand<T extends Validater> {
    public static final String COMMAND_HEADER = "$cmd=";

    private IOTCommandType commandType;

    private T parameters;

    private String apiKey;

    public IOTCommand(IOTCommandType commandType, String apiKey) {
        this.commandType = commandType;
        this.apiKey = TextUtils.isEmpty(apiKey) ? "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" : apiKey;
    }

    public IOTCommand(IOTCommandType commandType, T parameters, String apiKey) {
        this.commandType = commandType;
        this.parameters = parameters;
        this.apiKey = TextUtils.isEmpty(apiKey) ? "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" : apiKey;
    }

    @Override
    public String toString() {
//        if (parameters != null)
//            parameters.validate();

        String paraString = parameters == null ? "" : parameters.toString();

        return COMMAND_HEADER + commandType.toString() + "&"
                + paraString
                + "&apikey=" + apiKey
                + "&msgid=" + UUID.randomUUID().toString()
                + "&&";
    }
}
