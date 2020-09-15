package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetRainPrecisionInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析设置雨量计精度
 */
@Parser
public class SetRainPrecisionParser implements ResultParser<SetRainPrecisionInfo> {
    @Override
    public SetRainPrecisionInfo parse(String result) {
        SetRainPrecisionInfo info = new SetRainPrecisionInfo();
        info.setPrecision(Integer.parseInt(result.substring(5)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SETTING_RAIN_PRECISION;
    }
}
