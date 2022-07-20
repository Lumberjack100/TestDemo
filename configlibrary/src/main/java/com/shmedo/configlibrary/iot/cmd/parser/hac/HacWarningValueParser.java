package com.shmedo.configlibrary.iot.cmd.parser.hac;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.hac.HacWarningValue;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/20 <br/>
 * 描述：      解析 HAC 预警值参数
 */
public class HacWarningValueParser implements IOTResultParser<HacWarningValue> {
    @Override
    public HacWarningValue parse(String result) {
        HacWarningValue info = new HacWarningValue();
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
            info.setX1min(keyValueMap.getOrDefault("x1min", "NullKey"));
            info.setX1max(keyValueMap.getOrDefault("x1max", "NullKey"));
            info.setX1min(keyValueMap.getOrDefault("y1min", "NullKey"));
            info.setY1max(keyValueMap.getOrDefault("y1max", "NullKey"));
            info.setX2min(keyValueMap.getOrDefault("x2min", "NullKey"));
            info.setX2max(keyValueMap.getOrDefault("x2max", "NullKey"));
            info.setY3min(keyValueMap.getOrDefault("y2min", "NullKey"));
            info.setY2max(keyValueMap.getOrDefault("y2max", "NullKey"));
            info.setX3min(keyValueMap.getOrDefault("x3min", "NullKey"));
            info.setX3max(keyValueMap.getOrDefault("x3max", "NullKey"));
            info.setY3min(keyValueMap.getOrDefault("y3min", "NullKey"));
            info.setY3max(keyValueMap.getOrDefault("y3max", "NullKey"));

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
        return IOTCommandType.ADME_HAC_MD_GET_WARN;
    }
}
