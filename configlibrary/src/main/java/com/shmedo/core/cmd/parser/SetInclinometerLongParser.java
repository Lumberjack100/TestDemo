package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetInclinometerLongInfo;

/**
 * Created by adu on 2017/12/28.
 * 解析设置测斜仪测段长
 */
@Parser
public class SetInclinometerLongParser implements ResultParser<SetInclinometerLongInfo> {
    @Override
    public SetInclinometerLongInfo parse(String result) {
        SetInclinometerLongInfo info = new SetInclinometerLongInfo();
        info.setSensorType(SensorType.valueOf(result.substring(5,7)));
        info.setMeasSegment(result.substring(7));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_INCLINOMETER_LONG;
    }
}
