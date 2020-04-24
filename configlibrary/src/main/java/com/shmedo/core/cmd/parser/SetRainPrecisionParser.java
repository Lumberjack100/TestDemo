package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetRainPrecisionInfo;

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
