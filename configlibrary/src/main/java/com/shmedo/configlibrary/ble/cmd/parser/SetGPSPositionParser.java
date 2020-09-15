package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetGPSPositionInfo;

/**
 * Created by adu on 2017/12/18.
 * 解析GPS定位
 */
@Parser
public class SetGPSPositionParser implements ResultParser<SetGPSPositionInfo> {
    @Override
    public SetGPSPositionInfo parse(String result) {
        SetGPSPositionInfo info = new SetGPSPositionInfo();
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
