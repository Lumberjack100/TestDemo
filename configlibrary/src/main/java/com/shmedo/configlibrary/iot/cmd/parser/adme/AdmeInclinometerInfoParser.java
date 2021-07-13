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
            info.setInctype(!keyValueMap.containsKey("inctype") ? "NullKey" : keyValueMap.get("inctype"));
            info.setLowpower(!keyValueMap.containsKey("lowpower") ? "NullKey" : keyValueMap.get("lowpower"));
            info.setAddress(!keyValueMap.containsKey("address") ? "NullKey" : keyValueMap.get("address"));
            info.setCollinval(!keyValueMap.containsKey("collinval") ? "NullKey" : keyValueMap.get("collinval"));
            info.setCalcinval(!keyValueMap.containsKey("calcinval") ? "NullKey" : keyValueMap.get("calcinval"));
            info.setDormancytime(!keyValueMap.containsKey("dormancytime") ? "NullKey" : keyValueMap.get("dormancytime"));
            info.setInterupdate(!keyValueMap.containsKey("interupdate") ? "NullKey" : keyValueMap.get("interupdate"));

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
