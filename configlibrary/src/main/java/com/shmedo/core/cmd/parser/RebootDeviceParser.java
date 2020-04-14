package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.RebootDeviceInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析重启设备
 */
@Parser
public class RebootDeviceParser implements ResultParser<RebootDeviceInfo> {
    @Override
    public RebootDeviceInfo parse(String result) {
        RebootDeviceInfo info = new RebootDeviceInfo();
        info.setTime(Integer.valueOf(result.substring(5)));
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
