package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SettingGPSPositionInfo;

/**
 * Created by adu on 2017/12/18.
 * 解析GPS定位
 */
@Parser
public class SettingGPSPositionParser implements ResultParser<SettingGPSPositionInfo> {
    @Override
    public SettingGPSPositionInfo parse(String result) {
        SettingGPSPositionInfo info = new SettingGPSPositionInfo();
        String sensitivity = result.substring(5,7);
        info.setSensitivity(sensitivity);
        if (result.substring(7) != null){
            info.setAccuracy(Integer.valueOf(result.substring(7)));
        }
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SETTING_GPS_POSITION;
    }
}
