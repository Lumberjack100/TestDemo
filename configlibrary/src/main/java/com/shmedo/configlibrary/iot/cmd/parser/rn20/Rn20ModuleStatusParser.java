package com.shmedo.configlibrary.iot.cmd.parser.rn20;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.rn20.Rn20ModuleStatus;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/11/22 <br/>
 * 描述：     TODO
 */
public class Rn20ModuleStatusParser implements IOTResultParser<Rn20ModuleStatus> {
    @Override
    public Rn20ModuleStatus parse(String result) {
        Rn20ModuleStatus info = new Rn20ModuleStatus();
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
            info.setFlash(keyValueMap.getOrDefault("flash", "NullKey"));
            info.setAds(keyValueMap.getOrDefault("ads", "NullKey"));
            info.setBle(keyValueMap.getOrDefault("ble", "NullKey"));
            info.setLora(keyValueMap.getOrDefault("lora", "NullKey"));
            info.setVm501(keyValueMap.getOrDefault("vm501", "NullKey"));
            info.setAdxl362(keyValueMap.getOrDefault("adxl362", "NullKey"));
            info.setMmc5883(keyValueMap.getOrDefault("mmc5883", "NullKey"));
            info.setScl3300(keyValueMap.getOrDefault("scl3300", "NullKey"));
            info.setAht21(keyValueMap.getOrDefault("aht21", "NullKey"));
            info.setRtc(keyValueMap.getOrDefault("rtc", "NullKey"));
            info.setLtc2945(keyValueMap.getOrDefault("ltc2945", "NullKey"));

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
        return IOTCommandType.RN20_MD_GET_TERMINAL_MODULE_STATUS;
    }
}
