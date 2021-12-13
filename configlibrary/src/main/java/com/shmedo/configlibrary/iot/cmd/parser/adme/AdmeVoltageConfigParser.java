package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeVoltageConfigInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：     解析ADME电压配置参数
 */
public class AdmeVoltageConfigParser implements IOTResultParser<AdmeVoltageConfigInfo> {
    @Override
    public AdmeVoltageConfigInfo parse(String result) {
        AdmeVoltageConfigInfo info = new AdmeVoltageConfigInfo();
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
            info.setDriveovervolt(!keyValueMap.containsKey("driveovervolt") ? "NullKey" : keyValueMap.get("driveovervolt"));
            info.setDrivelowvolt(!keyValueMap.containsKey("drivelowvolt") ? "NullKey" : keyValueMap.get("drivelowvolt"));
            info.setDriveundervolt(!keyValueMap.containsKey("driveundervolt") ? "NullKey" : keyValueMap.get("driveundervolt"));
            info.setInclowvolt(!keyValueMap.containsKey("inclowvolt") ? "NullKey" : keyValueMap.get("inclowvolt"));
            info.setIncundervolt(!keyValueMap.containsKey("incundervolt") ? "NullKey" : keyValueMap.get("incundervolt"));

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
        return IOTCommandType.ADME_MD_GET_VOLTAGE;
    }
}
