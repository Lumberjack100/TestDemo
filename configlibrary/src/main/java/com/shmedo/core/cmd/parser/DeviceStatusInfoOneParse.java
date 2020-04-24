package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.DeviceStatusInfoOne;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    解析设备状态信息
 */
public class DeviceStatusInfoOneParse implements ResultParser<DeviceStatusInfoOne> {
    @Override
    public DeviceStatusInfoOne parse(String result) {
        String[] strs = result.split(",", -1);
        if (strs.length < 7) {
            return null;
        }
        DeviceStatusInfoOne statusOne = new DeviceStatusInfoOne();
        statusOne.setSnNumber(strs[1]);
        statusOne.setImeiNumber(strs[2]);
        statusOne.setSimNumber(strs[3]);
        statusOne.setStartCodeOne(strs[4]);
        statusOne.setStartCodeTwo(strs[5]);
        statusOne.setSignalStrength(strs[6]);
        return statusOne;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_DAS_STATUS_1;
    }
}
