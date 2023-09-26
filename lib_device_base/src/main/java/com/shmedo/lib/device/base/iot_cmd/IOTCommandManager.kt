package com.shmedo.lib.device.base.iot_cmd;


import com.shmedo.lib.device.base.Validater;
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/11 <br/>
 * 描述：     TODO #gh#
 */
public class IOTCommandManager {

    private static final IOTCommandManager instance = new IOTCommandManager();

    public static IOTCommandManager getInstance() {
        return instance;
    }

    private IOTCommandManager() {
    }

    public String getCommand(IOTCommandType commandType) {
        IOTCommand cmd = new IOTCommand(commandType);
        return cmd.toString();
    }

    public <T extends Validater> String getCommand(IOTCommandType commandType, T parameter) {
        IOTCommand cmd = new IOTCommand(commandType, parameter);
        return cmd.toString();
    }


}
