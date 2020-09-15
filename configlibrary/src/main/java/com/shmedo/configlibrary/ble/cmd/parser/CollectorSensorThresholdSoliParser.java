package com.shmedo.configlibrary.ble.cmd.parser;


import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CollectorSensorThresholdSoliInfo;

/**
 * Created by adu on 2017/12/21.
 * 解析 设置采集器接入传感器触发阈值————目前仅适用于墒情采集器
 */
@Parser
public class CollectorSensorThresholdSoliParser implements ResultParser<CollectorSensorThresholdSoliInfo> {
    @Override
    public CollectorSensorThresholdSoliInfo parse(String result) {
        CollectorSensorThresholdSoliInfo info = new CollectorSensorThresholdSoliInfo();
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
        return CommandType.COLLECTOR_SENSOR_THRESHOLD_SOLI;
    }
}
