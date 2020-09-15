package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorBaudRate;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SensorBaudRateInfo;

/**
 * Created by adu on 2017/12/18.
 */
@Parser
public class SensorBaudRateParser implements ResultParser<SensorBaudRateInfo> {
    @Override
    public SensorBaudRateInfo parse(String result) {
        SensorBaudRateInfo info = new SensorBaudRateInfo();
        info.setSensorBaudRate(SensorBaudRate.value(result.substring(5)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SENSOR_BAUD_RATE;
    }
}
