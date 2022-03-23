package com.shmedo.configlibrary.iot.cmd.parser.das;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.AudibleAlarm;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：    解析声光报警参数
 */
public class AudibleAlarmParser implements IOTResultParser<AudibleAlarm> {
    @Override
    public AudibleAlarm parse(String result) {
        AudibleAlarm info = new AudibleAlarm();
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
            info.setChannel(keyValueMap.getOrDefault("channel", "NullKey"));
            info.setPanid(keyValueMap.getOrDefault("panid", "NullKey"));
            info.setGroupid(keyValueMap.getOrDefault("groupid", "NullKey"));
            info.setAlarmtype(keyValueMap.getOrDefault("alarmtype", "NullKey"));

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
        return IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM;
    }
}
