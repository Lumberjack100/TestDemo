package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/2/21 <br/>
 * 描述：      解析ADME 电机运动堵转缓停参数
 */
public class AdmeLockedRotorDetectionInfoParser implements IOTResultParser<AdmeLockedRotorDetectionInfo> {
    @Override
    public AdmeLockedRotorDetectionInfo parse(String result) {
        AdmeLockedRotorDetectionInfo info = new AdmeLockedRotorDetectionInfo();
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
            info.setLowtbtss(keyValueMap.getOrDefault("lowtbtss", "NullKey"));
            info.setNumpput(keyValueMap.getOrDefault("numpput", "NullKey"));
            info.setPdajtime(keyValueMap.getOrDefault("pdajtime", "NullKey"));
            info.setDetintiona(keyValueMap.getOrDefault("detintiona", "NullKey"));
            info.setDetintionb(keyValueMap.getOrDefault("detintionb", "NullKey"));
            info.setLowtorblothr(keyValueMap.getOrDefault("lowtorblothr", "NullKey"));
            info.setLowtordetime(keyValueMap.getOrDefault("lowtordetime", "NullKey"));
            info.setLowsusrana(keyValueMap.getOrDefault("lowsusrana", "NullKey"));
            info.setLowsusranb(keyValueMap.getOrDefault("lowsusranb", "NullKey"));
            info.setUptbtss(keyValueMap.getOrDefault("uptbtss", "NullKey"));
            info.setUptorblothr(keyValueMap.getOrDefault("uptorblothr", "NullKey"));
            info.setUptordetime(keyValueMap.getOrDefault("uptordetime", "NullKey"));
            info.setUpsusrana(keyValueMap.getOrDefault("upsusrana", "NullKey"));
            info.setUpsusranb(keyValueMap.getOrDefault("upsusranb", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION;
    }
}
