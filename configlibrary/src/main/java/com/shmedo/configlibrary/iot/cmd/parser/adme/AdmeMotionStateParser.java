package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotionState;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：      解析ADME运行状态
 */
public class AdmeMotionStateParser implements IOTResultParser<AdmeMotionState> {
    @Override
    public AdmeMotionState parse(String result) {
        AdmeMotionState info = new AdmeMotionState();
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
            info.setMotionstate(keyValueMap.getOrDefault("motionstate", "NullKey"));
            info.setInctiondis(keyValueMap.getOrDefault("inctiondis", "NullKey"));
            info.setMeasmode(keyValueMap.getOrDefault("measmode", "NullKey"));
            info.setMotorinfo(keyValueMap.getOrDefault("motorinfo", "NullKey"));
            info.setMeaspoint(keyValueMap.getOrDefault("measpoint", "NullKey"));
            info.setWaittime(keyValueMap.getOrDefault("waittime", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_MOTION_STATE;
    }
}
