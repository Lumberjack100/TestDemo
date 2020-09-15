package com.shmedo.configlibrary.iot.parser;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     TODO
 */
public class DeviceCurrentStateParser implements IOTResultParser<String> {
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
        return IOTCommandType.QUERY_DEVICE_STATUS;
    }
}
