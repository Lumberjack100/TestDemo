package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetOsmometerTriggerInfo;

/**
 * Created by adu on 2018/1/8.
 * 解析数字水位计深度、温度触发值
 */
@Parser
public class SetOsmometerTriggerParser implements ResultParser<SetOsmometerTriggerInfo> {
    @Override
    public SetOsmometerTriggerInfo parse(String result) {
        SetOsmometerTriggerInfo info = new SetOsmometerTriggerInfo();
        String [] strs = result.split(",");
        info.setDepthTrigger(Integer.valueOf(strs[0].substring(5)));
        info.setTemperatureTrigger(Integer.valueOf(strs[1]));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_OSMOMETER_TRIGGER;
    }
}
