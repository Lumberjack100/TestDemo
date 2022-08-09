package com.shmedo.configlibrary.iot.cmd.parser.hac;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.hac.HacMotorMotionDistanceInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/25 <br/>
 * 描述：      解析电机实时运动数据
 */
public class HacMotorMotionDistanceInfoParser implements IOTResultParser<HacMotorMotionDistanceInfo> {
    @Override
    public HacMotorMotionDistanceInfo parse(String result) {
        HacMotorMotionDistanceInfo info = new HacMotorMotionDistanceInfo();
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
            info.setPulsenumber(keyValueMap.getOrDefault("pulsenumber", "NullKey"));
            info.setRealmovedistance(keyValueMap.getOrDefault("realmovedistance", "NullKey"));
            info.setRealholedepth(keyValueMap.getOrDefault("realholedepth", "NullKey"));
            info.setAbndiasis(keyValueMap.getOrDefault("abndiasis", "NullKey"));

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
        return IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE;
    }
}
