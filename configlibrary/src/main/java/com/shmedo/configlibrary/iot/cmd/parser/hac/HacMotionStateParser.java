package com.shmedo.configlibrary.iot.cmd.parser.hac;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.hac.HacMotionState;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/15 <br/>
 * 描述：     解析 HAC 电机运动状态
 */
public class HacMotionStateParser implements IOTResultParser<HacMotionState> {
    @Override
    public HacMotionState parse(String result) {
        HacMotionState info = new HacMotionState();
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
            info.setAbndiasis(keyValueMap.getOrDefault("abndiasis", "NullKey"));
            info.setMeasmode(keyValueMap.getOrDefault("measmode", "NullKey"));
            info.setMotorinfo(keyValueMap.getOrDefault("motorinfo", "NullKey"));
            info.setMeaspoint(keyValueMap.getOrDefault("measpoint", "NullKey"));
            info.setWaittime(keyValueMap.getOrDefault("waittime", "NullKey"));
            info.setIncvoltage(keyValueMap.getOrDefault("incvoltage", "NullKey"));
            info.setDriveinputv(keyValueMap.getOrDefault("driveinputv", "NullKey"));

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
        return IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE;
    }
}
