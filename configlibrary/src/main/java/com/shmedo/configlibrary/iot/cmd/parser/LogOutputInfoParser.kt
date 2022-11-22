package com.shmedo.configlibrary.iot.cmd.parser;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.IotLogOutputInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     解析设备日志输出方式信息
 */
public class LogOutputInfoParser implements IOTResultParser<IotLogOutputInfo> {
    @Override
    public IotLogOutputInfo parse(String result) {
        IotLogOutputInfo info = new IotLogOutputInfo();
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
            info.setLevel(keyValueMap.get("level"));
            info.setType(keyValueMap.get("type"));

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
        return IOTCommandType.GET_LOG_OUTPUT_MODE_LEVEL;
    }
}
