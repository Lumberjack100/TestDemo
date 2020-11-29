package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.RainStation;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.RainStationInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析雨量站
 */
@Parser
public class RainStationParser implements ResultParser<RainStationInfo> {

    @Override
    public RainStationInfo parse(String result) {
        RainStationInfo info = new RainStationInfo();
        info.setRainStation(RainStation.value(Integer.parseInt(result.substring(5))));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.RAIN_STATION;
    }
}
