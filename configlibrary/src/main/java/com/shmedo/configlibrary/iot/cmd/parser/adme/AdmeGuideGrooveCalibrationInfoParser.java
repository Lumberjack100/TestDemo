package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeGuideGrooveCalibrationInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：     解析ADME导槽校准配置参数
 */
public class AdmeGuideGrooveCalibrationInfoParser implements IOTResultParser<AdmeGuideGrooveCalibrationInfo> {
    @Override
    public AdmeGuideGrooveCalibrationInfo parse(String result) {
        AdmeGuideGrooveCalibrationInfo info = new AdmeGuideGrooveCalibrationInfo();
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
            info.setMorunstate(keyValueMap.getOrDefault("morunstate", "NullKey"));
            info.setMovementway(keyValueMap.getOrDefault("movementway", "NullKey"));
            info.setMotorspeed(keyValueMap.getOrDefault("motorspeed", "NullKey"));
            info.setMovePulse(keyValueMap.getOrDefault("movepulse", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION;
    }
}
