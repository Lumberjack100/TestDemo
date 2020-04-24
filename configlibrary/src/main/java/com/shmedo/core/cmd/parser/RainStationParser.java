package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.RainStation;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.RainStationInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析雨量站
 */
@Parser
public class RainStationParser implements ResultParser<RainStationInfo> {

    @Override
    public RainStationInfo parse(String result) {
        RainStationInfo info = new RainStationInfo();
        info.setRainStation(RainStation.valueOf(Integer.parseInt(result.substring(5))));
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
