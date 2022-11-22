package com.shmedo.configlibrary.iot.cmd.parser;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     解析运行状态数据(扩展指令)
 */
public class DeviceCurrentStateExParser implements IOTResultParser<String> {
    @Override
    public String parse(String result) {
        String info;

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
            info = keyValueMap.get("status");
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
        return IOTCommandType.QUERY_DEVICE_EX_STATUS;
    }
}
