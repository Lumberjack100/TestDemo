package com.shmedo.core.cmd.parser;


import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.LowEnergyModel;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.LowEnergyModelInfo;

/**
 * Created by adu on 2017/12/18.
 * 解析设置DAS工作模式
 */
@Parser
public class LowEnergyModelParser implements ResultParser<LowEnergyModelInfo> {
    @Override
    public LowEnergyModelInfo parse(String result) {
        result.replace("\r\n","");
        LowEnergyModelInfo info = new LowEnergyModelInfo();
        info.setLowEnergyModel(LowEnergyModel.valueOf(Integer.parseInt(result.substring(5))));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.LOW_ENERGY;
    }
}
