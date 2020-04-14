package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.SensorInterfaceType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SensorInterfaceTypeInfo;

/**
 * Created by adu on 2017/12/18.
 * 解析传感器接口类型
 */
@Parser
public class SensorInterfaceTypeParser implements ResultParser<SensorInterfaceTypeInfo> {
    @Override
    public SensorInterfaceTypeInfo parse(String result) {
        SensorInterfaceTypeInfo info = new SensorInterfaceTypeInfo();
        info.setType(SensorInterfaceType.valueOf(Integer.valueOf(result.substring(5))));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SENSOR_INTERFACE_TYPE;
    }
}
