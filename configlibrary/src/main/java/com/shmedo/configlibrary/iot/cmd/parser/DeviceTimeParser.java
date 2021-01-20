package com.shmedo.configlibrary.iot.cmd.parser;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.DeviceTimeInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：    解析设备终端时间
 */
public class DeviceTimeParser implements IOTResultParser<DeviceTimeInfo> {
    @Override
    public DeviceTimeInfo parse(String result) {
        DeviceTimeInfo info = new DeviceTimeInfo();
        try {
            String[] keyValues = result.split("&");
            HashMap<String, String> keyValueMap = new HashMap<>();

            for (String keyValue : keyValues) {
                String[] strs = keyValue.split("=");
                if (strs.length < 2) {
                    keyValueMap.put(strs[0], "");
                } else {
                    keyValueMap.put(strs[0], strs[1]);
                }
            }
            info.setTime(keyValueMap.get("time"));

            return info;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public IOTCommandType commandType() {
        return IOTCommandType.QUERY_TERMINAL_TIME;
    }
}
