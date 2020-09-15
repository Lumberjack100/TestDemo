package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.DeviceNetStatus;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    解析设备网络状态
 */
public class DeviceNetStatusParser implements ResultParser<DeviceNetStatus> {
    @Override
    public DeviceNetStatus parse(String result) {
        String[] cmd = result.split(",", -1);
        DeviceNetStatus netStatus = new DeviceNetStatus();
        String linkNumber = cmd[0].substring(5, 6);
        netStatus.setLinkNumber(linkNumber);
        netStatus.setSentData(cmd[1]);
        netStatus.setGeneratedData(cmd[2]);
        netStatus.setFlashEnable(cmd[3]);
        netStatus.setFlashReadPointer(cmd[4]);
        netStatus.setFlashWritePointer(cmd[5]);
        netStatus.setLinkEnable(cmd[6]);
        netStatus.setLinkStatus(cmd[7]);
        netStatus.setFourGModuleStatus(cmd[8]);
        netStatus.setMqttStatus(cmd[9]);
        if (cmd.length == 11)
            netStatus.setOnlineRate(cmd[10]);

        return netStatus;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_NETWORK_STATUS;
    }
}
