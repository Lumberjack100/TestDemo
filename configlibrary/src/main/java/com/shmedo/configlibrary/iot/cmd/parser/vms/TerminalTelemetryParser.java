package com.shmedo.configlibrary.iot.cmd.parser.vms;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/11/21 <br/>
 * 描述：     解析网关终端设备遥测数据
 */
public class TerminalTelemetryParser implements IOTResultParser<String> {
    @Override
    public String parse(String result) {
        String[] strs = result.split("&");
        return strs[1];
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public IOTCommandType commandType() {
        return IOTCommandType.VMS_TERMINAL_QUERY_SAMPLE;
    }
}
