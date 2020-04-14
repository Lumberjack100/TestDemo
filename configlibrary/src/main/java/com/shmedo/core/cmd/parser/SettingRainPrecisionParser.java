package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SettingRainPrecisionInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析设置雨量计精度
 */
@Parser
public class SettingRainPrecisionParser implements ResultParser<SettingRainPrecisionInfo> {
    @Override
    public SettingRainPrecisionInfo parse(String result) {
        SettingRainPrecisionInfo info = new SettingRainPrecisionInfo();
        info.setPrecision(Integer.valueOf(result.substring(5)));
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
