package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeInclinometerInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：    解析ADME测斜仪配置参数
 */
public class AdmeInclinometerInfoParser implements IOTResultParser<AdmeInclinometerInfo> {
    @Override
    public AdmeInclinometerInfo parse(String result) {
        AdmeInclinometerInfo info = new AdmeInclinometerInfo();
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
            info.setInctype(keyValueMap.getOrDefault("inctype", "NullKey"));
            info.setLowpower(keyValueMap.getOrDefault("lowpower", "NullKey"));
            info.setAddress(keyValueMap.getOrDefault("address", "NullKey"));
            info.setCollinval(keyValueMap.getOrDefault("collinval", "NullKey"));
            info.setCalcinval(keyValueMap.getOrDefault("calcinval", "NullKey"));
            info.setDormancytime(keyValueMap.getOrDefault("dormancytime", "NullKey"));
            info.setInterupdate(keyValueMap.getOrDefault("interupdate", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_INCLINOMETER;
    }
}
