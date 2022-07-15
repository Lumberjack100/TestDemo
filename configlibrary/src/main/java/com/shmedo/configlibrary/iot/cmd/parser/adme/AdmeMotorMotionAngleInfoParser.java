package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionAngleInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：     解析电机实时运动数据
 */
public class AdmeMotorMotionAngleInfoParser implements IOTResultParser<AdmeMotorMotionAngleInfo> {
    @Override
    public AdmeMotorMotionAngleInfo parse(String result) {
        AdmeMotorMotionAngleInfo info = new AdmeMotorMotionAngleInfo();
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
            info.setRealmoveangle(keyValueMap.getOrDefault("realmoveangle", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE;
    }
}
