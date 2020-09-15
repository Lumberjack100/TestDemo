package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetInclinometerLongInfo;

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
