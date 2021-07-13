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
            info.setLowtbtss(!keyValueMap.containsKey("lowtbtss") ? "NullKey"  : keyValueMap.get("lowtbtss"));
            info.setNumpput(!keyValueMap.containsKey("numpput") ? "NullKey" : keyValueMap.get("numpput"));
            info.setPdajtime(!keyValueMap.containsKey("pdajtime") ? "NullKey"  : keyValueMap.get("pdajtime"));
            info.setDetintiona(!keyValueMap.containsKey("detintiona") ? "NullKey"  : keyValueMap.get("detintiona"));
            info.setDetintionb(!keyValueMap.containsKey("detintionb") ? "NullKey"  : keyValueMap.get("detintionb"));
            info.setLowtorblothr(!keyValueMap.containsKey("lowtorblothr") ? "NullKey"  : keyValueMap.get("lowtorblothr"));
            info.setLowtordetime(!keyValueMap.containsKey("lowtordetime") ? "NullKey"  : keyValueMap.get("lowtordetime"));
            info.setLowsusrana(!keyValueMap.containsKey("lowsusrana") ? "NullKey"  : keyValueMap.get("lowsusrana"));
            info.setLowsusranb(!keyValueMap.containsKey("lowsusranb") ? "NullKey"  : keyValueMap.get("lowsusranb"));
            info.setUptbtss(!keyValueMap.containsKey("uptbtss") ? "NullKey"  : keyValueMap.get("uptbtss"));
            info.setUptorblothr(!keyValueMap.containsKey("uptorblothr") ? "NullKey"  : keyValueMap.get("uptorblothr"));
            info.setUptordetime(!keyValueMap.containsKey("uptordetime") ? "NullKey" : keyValueMap.get("uptordetime"));
            info.setUpsusrana(!keyValueMap.containsKey("upsusrana") ? "NullKey"  : keyValueMap.get("upsusrana"));
            info.setUpsusranb(!keyValueMap.containsKey("upsusranb") ? "NullKey"  : keyValueMap.get("upsusranb"));

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
