package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：   解析设备安装位置
 */
public class InstallLocationParser implements ResultParser<String> {
    @Override
    public String parse(String result) {
        return result.substring(7);
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
       return CommandType.INSTALL_LOCATION;
    }
}
