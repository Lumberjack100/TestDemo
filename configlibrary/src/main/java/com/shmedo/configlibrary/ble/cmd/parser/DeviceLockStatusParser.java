package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.DeviceLockStatus;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.DeviceLockStatusInfo;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.das.cmd.parser
 * 文件名:   DeviceLockStatusParser
 * 创建者:   dpc
 * 创建时间:  2019/4/26 09:20
 * 描述：    设备锁状态解析类
 */

public class DeviceLockStatusParser implements ResultParser<DeviceLockStatusInfo> {
    @Override public DeviceLockStatusInfo parse(String result) {
        DeviceLockStatusInfo info = new DeviceLockStatusInfo();
        info.setStatus(DeviceLockStatus.valueOf(Integer.parseInt(result.substring(5))));
        return info;
    }


    @Override public void validate(String result) {

    }


    @Override public CommandType commandType() {
        return CommandType.DEVICE_LOCK_STATUS;
    }
}
