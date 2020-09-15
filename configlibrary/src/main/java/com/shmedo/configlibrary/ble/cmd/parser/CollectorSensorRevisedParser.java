package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CollectorSensorRevisedInfo;

/**
 * Created by adu on 2017/12/21.
 * 解析设置采集器接入传感器修正值
 */
@Parser
public class CollectorSensorRevisedParser implements ResultParser<CollectorSensorRevisedInfo> {
    @Override
    public CollectorSensorRevisedInfo parse(String result) {
        CollectorSensorRevisedInfo info = new CollectorSensorRevisedInfo();
        String[] strs = result.split(",");
        info.setCollectorModel(CollectorModel.value(strs[0].substring(5,7)));
        info.setAddress(strs[0].substring(7));
        info.setHumidity(strs[1]);
        info.setSalinity(strs[2]);
        info.setTemperature(strs[3]);
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_SENSOR_REVISED;
    }
}
