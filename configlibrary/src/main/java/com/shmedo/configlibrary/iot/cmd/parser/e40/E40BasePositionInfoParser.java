package com.shmedo.configlibrary.iot.cmd.parser.e40;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.e40.E40BasePositionInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/2 <br/>
 * 描述：    解析 E40基站位置信息
 */
public class E40BasePositionInfoParser implements IOTResultParser<E40BasePositionInfo> {
    @Override
    public E40BasePositionInfo parse(String result) {
        E40BasePositionInfo info = new E40BasePositionInfo();
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
            info.setMode(keyValueMap.getOrDefault("mode", "NullKey"));
            info.setLon(keyValueMap.getOrDefault("lon", "NullKey"));
            info.setLat(keyValueMap.getOrDefault("lat", "NullKey"));
            info.setAlt(keyValueMap.getOrDefault("alt", "NullKey"));

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
        return IOTCommandType.E40_MD_GET_BASE_POSITION;
    }
}
