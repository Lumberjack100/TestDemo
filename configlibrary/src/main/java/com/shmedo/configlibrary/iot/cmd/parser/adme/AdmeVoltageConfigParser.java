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
            info.setVolt_power_standard(keyValueMap.getOrDefault("volt_power_standard", "NullKey"));
            info.setVolt_power_low(keyValueMap.getOrDefault("volt_power_low", "NullKey"));
            info.setVolt_power_under(keyValueMap.getOrDefault("volt_power_under", "NullKey"));
            info.setVolt_sensor_standard(keyValueMap.getOrDefault("volt_sensor_standard", "NullKey"));
            info.setVolt_sensor_low(keyValueMap.getOrDefault("volt_sensor_low", "NullKey"));
            info.setVolt_sensor_under(keyValueMap.getOrDefault("volt_sensor_under", "NullKey"));
            info.setRope_length(keyValueMap.getOrDefault("rope_length", "NullKey"));

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
