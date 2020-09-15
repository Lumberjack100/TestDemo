package com.shmedo.configlibrary.iot.parser;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.TerminalTime;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：    解析设备终端时间
 */
public class TerminalTimeParser implements IOTResultParser<TerminalTime> {
    @Override
    public TerminalTime parse(String result) {
        TerminalTime info = new TerminalTime();
        String[] strs = result.split("&");
        String time = strs[1].replace("time=", "");
        info.setTime(time);
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public IOTCommandType commandType() {
        return IOTCommandType.QUERY_TERMINAL_TIME;
    }
}
