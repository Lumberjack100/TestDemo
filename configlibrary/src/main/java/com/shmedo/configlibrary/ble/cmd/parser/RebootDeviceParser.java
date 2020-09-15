package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.RebootDeviceInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析重启设备
 */
@Parser
public class RebootDeviceParser implements ResultParser<RebootDeviceInfo> {
    @Override
    public RebootDeviceInfo parse(String result) {
        RebootDeviceInfo info = new RebootDeviceInfo();
        info.setTime(Integer.parseInt(result.substring(5)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.REBOOT_DEVICE;
    }
}
