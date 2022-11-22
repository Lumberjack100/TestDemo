package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeCurrentStateInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：      解析 ADME 设备当前状态
 */
public class AdmeCurrentStateInfoParser implements IOTResultParser<AdmeCurrentStateInfo> {
    @Override
    public AdmeCurrentStateInfo parse(String result) {
        AdmeCurrentStateInfo info = new AdmeCurrentStateInfo();
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
            info.setSn(keyValueMap.getOrDefault("sn", "NullKey"));
            info.setProductid(keyValueMap.getOrDefault("productid", "NullKey"));
            info.setSimid(keyValueMap.getOrDefault("simid", "NullKey"));
            info.setImeid(keyValueMap.getOrDefault("imeid", "NullKey"));
            info.setFirversion(keyValueMap.getOrDefault("firversion", "NullKey"));
            info.setCtrinputv(keyValueMap.getOrDefault("ctrinputv", "NullKey"));
            info.setDriveinputv(keyValueMap.getOrDefault("driveinputv", "NullKey"));
            info.setInctype(keyValueMap.getOrDefault("inctype", "NullKey"));
            info.setIncnum(keyValueMap.getOrDefault("incnum", "NullKey"));
            info.setIncvoltage(keyValueMap.getOrDefault("incvoltage", "NullKey"));
            info.setTemperature(keyValueMap.getOrDefault("temperature", "NullKey"));
            info.setHumidity(keyValueMap.getOrDefault("humidity", "NullKey"));
            info.setIntertempe(keyValueMap.getOrDefault("intertempe", "NullKey"));
            info.setSignalstr(keyValueMap.getOrDefault("signalstr", "NullKey"));
            info.setIncloc(keyValueMap.getOrDefault("incloc", "NullKey"));
            info.setAbndiasis(keyValueMap.getOrDefault("abndiasis", "NullKey"));
            info.setDownnum(keyValueMap.getOrDefault("downnum", "NullKey"));
            info.setTestway(keyValueMap.getOrDefault("testway", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE;
    }
}
