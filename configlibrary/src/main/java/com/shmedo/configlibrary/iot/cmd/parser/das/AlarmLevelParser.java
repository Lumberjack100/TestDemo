package com.shmedo.configlibrary.iot.cmd.parser.das;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.AlarmLevel;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：     解析声光报警级别参数
 */
public class AlarmLevelParser implements IOTResultParser<AlarmLevel> {
    @Override
    public AlarmLevel parse(String result) {
        AlarmLevel info = new AlarmLevel();
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
            info.setType(keyValueMap.getOrDefault("type", "NullKey"));
            info.setLevel1(keyValueMap.getOrDefault("level1", "NullKey"));
            info.setLevel2(keyValueMap.getOrDefault("level2", "NullKey"));
            info.setLevel3(keyValueMap.getOrDefault("level3", "NullKey"));
            info.setLevel4(keyValueMap.getOrDefault("level4", "NullKey"));
            info.setLevel5(keyValueMap.getOrDefault("level5", "NullKey"));

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
        return IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM_LEVEL;
    }
}
