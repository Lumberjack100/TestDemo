package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：    解析ADME步进电机参数
 */
public class AdmeStepperMotorInfoParser implements IOTResultParser<AdmeStepperMotorInfo> {
    @Override
    public AdmeStepperMotorInfo parse(String result) {
        AdmeStepperMotorInfo info = new AdmeStepperMotorInfo();
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
            info.setPosnegtest(keyValueMap.getOrDefault("posnegtest", "NullKey"));
            info.setAbsprsion(keyValueMap.getOrDefault("absprsion", "NullKey"));
            info.setMovspeed(keyValueMap.getOrDefault("movspeed", "NullKey"));
            info.setMovesm(keyValueMap.getOrDefault("movesm", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_STEPPER_MOTOR;
    }
}
