package com.shmedo.core.cmd.parser;


import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.CollectorSensorThresholdInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析设置采集器传感器的触发阈值
 */
@Parser
public class CollectorSensorThresholdParser implements ResultParser<CollectorSensorThresholdInfo> {
    @Override
    public CollectorSensorThresholdInfo parse(String result) {
        CollectorSensorThresholdInfo info = new CollectorSensorThresholdInfo();
        info.setType(result.substring(5,7));
        info.setThreshold(result.substring(7));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_SENSOR_THRESHOLD;
    }
}
