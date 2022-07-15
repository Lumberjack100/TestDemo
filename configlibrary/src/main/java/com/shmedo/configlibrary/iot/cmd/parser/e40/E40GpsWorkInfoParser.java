package com.shmedo.configlibrary.iot.cmd.parser.e40;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.e40.E40GpsWorkInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/6/18 <br/>
 * 描述：     解析 GPS 工作参数
 */
public class E40GpsWorkInfoParser implements IOTResultParser<E40GpsWorkInfo> {
    @Override
    public E40GpsWorkInfo parse(String result) {
        E40GpsWorkInfo info = new E40GpsWorkInfo();
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
            info.setCutoffangle(keyValueMap.getOrDefault("cutoffangle", "NullKey"));
            info.setRange(keyValueMap.getOrDefault("range", "NullKey"));
            info.setSavefreq(keyValueMap.getOrDefault("savefreq", "NullKey"));

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
        return IOTCommandType.E40_MD_GET_GPS_PARAM;
    }
}
