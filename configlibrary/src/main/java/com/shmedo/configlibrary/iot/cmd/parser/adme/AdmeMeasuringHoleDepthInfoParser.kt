package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeasuringHoleDepthInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/6/21 <br/>
 * 描述：      解析ADME测量孔深配置参数
 */
public class AdmeMeasuringHoleDepthInfoParser implements IOTResultParser<AdmeMeasuringHoleDepthInfo> {
    @Override
    public AdmeMeasuringHoleDepthInfo parse(String result) {
        AdmeMeasuringHoleDepthInfo info = new AdmeMeasuringHoleDepthInfo();
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
            info.setMovedistance(keyValueMap.getOrDefault("movedistance", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH;
    }
}
