package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetCollectorSensorInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析 设置采集器接入的传感器
 */
@Parser
public class SetCollectorSensorParser implements ResultParser<SetCollectorSensorInfo> {
    @Override
    public SetCollectorSensorInfo parse(String result) {
        SetCollectorSensorInfo info = new SetCollectorSensorInfo();
        info.setNumber(CollectorModel.value(result.substring(5,7)));
        info.setAddressType(result.substring(7));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_COLLECTOR_SENSOR;
    }
}
