package com.shmedo.configlibrary.ble.cmd.parser;


import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SystemRunStateInfo;

/**
 * Created by adu on 2017/12/14.
 * 解析系统运行状态
 */
@Parser
public class SystemRunStateParser implements ResultParser<SystemRunStateInfo> {

    @Override
    public SystemRunStateInfo parse(String result) {

        String [] strs = result.split(",");
        if (strs.length == 11) {
            return startParser(strs);
        } else {
            throw new DASParameterException("参数错误");
        }

    }

    private SystemRunStateInfo startParser(String[] strs) {
        SystemRunStateInfo info = new SystemRunStateInfo();
        info.setGprsSignal(strs[1]);
        info.setGpsNumber(strs[2]);
        info.setSystemStartUp(strs[3]);
        info.setSystemRestart(strs[4]);
        info.setSimCCID(strs[5]);
        info.setInternalTemperature(strs[6]);
        info.setBatteryVoltage(strs[7]);
        info.setExternalVoltage(strs[8]);
        info.setOperator(strs[9]);
        info.setNetworkMode(strs[10]);
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SYSTEM_RUN_STATE;
    }
}
