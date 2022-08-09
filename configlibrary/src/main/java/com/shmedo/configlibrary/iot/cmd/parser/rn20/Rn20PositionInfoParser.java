package com.shmedo.configlibrary.iot.cmd.parser.rn20;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.rn20.Rn20PositionInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/4 <br/>
 * 描述：     解析雨量采集器经纬度信息
 */
public class Rn20PositionInfoParser implements IOTResultParser<Rn20PositionInfo> {
    @Override
    public Rn20PositionInfo parse(String result) {
        Rn20PositionInfo info = new Rn20PositionInfo();
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
            info.setLongitude(keyValueMap.getOrDefault("longitude", "NullKey"));
            info.setLatitude(keyValueMap.getOrDefault("latitude", "NullKey"));

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
        return IOTCommandType.RN20_MD_GET_TERMINAL_LOCAL;
    }
}
