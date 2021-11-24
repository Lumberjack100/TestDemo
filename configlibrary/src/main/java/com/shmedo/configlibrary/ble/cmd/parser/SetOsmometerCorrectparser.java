package com.shmedo.configlibrary.ble.cmd.parser;


import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetOsmometerCorrectInfo;

/**
 * Created by adu on 2018/1/8.
 * 解析数字水位计深度温度修正值
 */
@Parser
public class SetOsmometerCorrectparser implements ResultParser<SetOsmometerCorrectInfo> {
    @Override
    public SetOsmometerCorrectInfo parse(String result) {
        SetOsmometerCorrectInfo info = new SetOsmometerCorrectInfo();
        String [] strs = result.split(",");
        info.setDepthCorrect(Integer.valueOf(strs[0].substring(5)));
        info.setTemperatureCorrect(Integer.valueOf(strs[1]));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_OSMOMETR_CORRECT;
    }
}
