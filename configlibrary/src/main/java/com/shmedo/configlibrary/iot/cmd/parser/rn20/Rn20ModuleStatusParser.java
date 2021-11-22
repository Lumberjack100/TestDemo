package com.shmedo.configlibrary.iot.cmd.parser.rn20;

import android.text.TextUtils;

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
            info.setSn(TextUtils.isEmpty(keyValueMap.get("sn")) ? "" : keyValueMap.get("sn"));
            info.setFlash(TextUtils.isEmpty(keyValueMap.get("flash")) ? "" : keyValueMap.get("flash"));
            info.setAds(TextUtils.isEmpty(keyValueMap.get("ads")) ? "" : keyValueMap.get("ads"));
            info.setBle(TextUtils.isEmpty(keyValueMap.get("ble")) ? "" : keyValueMap.get("ble"));
            info.setLora(TextUtils.isEmpty(keyValueMap.get("lora")) ? "" : keyValueMap.get("lora"));
            info.setVm501(TextUtils.isEmpty(keyValueMap.get("vm501")) ? "" : keyValueMap.get("vm501"));
            info.setAdxl362(TextUtils.isEmpty(keyValueMap.get("adxl362")) ? "" : keyValueMap.get("adxl362"));
            info.setMmc5883(TextUtils.isEmpty(keyValueMap.get("mmc5883")) ? "" : keyValueMap.get("mmc5883"));
            info.setScl3300(TextUtils.isEmpty(keyValueMap.get("scl3300")) ? "" : keyValueMap.get("scl3300"));
            info.setAht21(TextUtils.isEmpty(keyValueMap.get("aht21")) ? "" : keyValueMap.get("aht21"));
            info.setRtc(TextUtils.isEmpty(keyValueMap.get("rtc")) ? "" : keyValueMap.get("rtc"));
            info.setLtc2945(TextUtils.isEmpty(keyValueMap.get("ltc2945")) ? "" : keyValueMap.get("ltc2945"));

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
