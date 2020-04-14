package com.shmedo.core.cmd.parser;


import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetOsmometerCorrectInfo;

/**
 * Created by adu on 2018/1/8.
 * 解析数字渗压计深度温度修正值
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
