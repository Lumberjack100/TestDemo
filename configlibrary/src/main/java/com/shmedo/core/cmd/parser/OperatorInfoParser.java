package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.OperatorInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    TODO
 */
public class OperatorInfoParser implements ResultParser<OperatorInfo> {
    @Override
    public OperatorInfo parse(String result) {
        String[] cmd = result.split(",", -1);
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setSignalStrength(Integer.parseInt(cmd[1]));
        operatorInfo.setGPSSearchStars(cmd[2]);
        operatorInfo.setStartupCode1(cmd[3]);
        operatorInfo.setStartupCode2(cmd[4]);
        operatorInfo.setSIMCardNumber(cmd[5]);
        operatorInfo.setMCUTemperature(cmd[6]);
        operatorInfo.setDeviceInternalVoltage(cmd[7]);
        operatorInfo.setDeviceExternalVoltage(cmd[8]);
        operatorInfo.setOperatorType(cmd[9]);
        operatorInfo.setNetworkType(cmd[10]);

        return operatorInfo;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SYSTEM_RUN_STATE;
    }
}
