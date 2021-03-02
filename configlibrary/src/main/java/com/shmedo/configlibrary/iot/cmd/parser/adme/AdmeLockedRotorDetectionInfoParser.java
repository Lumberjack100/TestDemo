package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

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
            info.setLowtbtss(TextUtils.isEmpty(keyValueMap.get("lowtbtss")) ? "" : keyValueMap.get("lowtbtss"));
            info.setNumpput(TextUtils.isEmpty(keyValueMap.get("numpput")) ? "" : keyValueMap.get("numpput"));
            info.setPdajtime(TextUtils.isEmpty(keyValueMap.get("pdajtime")) ? "" : keyValueMap.get("pdajtime"));
            info.setDetintiona(TextUtils.isEmpty(keyValueMap.get("detintiona")) ? "" : keyValueMap.get("detintiona"));
            info.setDetintionb(TextUtils.isEmpty(keyValueMap.get("detintionb")) ? "" : keyValueMap.get("detintionb"));
            info.setLowtorblothr(TextUtils.isEmpty(keyValueMap.get("lowtorblothr")) ? "" : keyValueMap.get("lowtorblothr"));
            info.setLowtordetime(TextUtils.isEmpty(keyValueMap.get("lowtordetime")) ? "" : keyValueMap.get("lowtordetime"));
            info.setLowsusrana(TextUtils.isEmpty(keyValueMap.get("lowsusrana")) ? "" : keyValueMap.get("lowsusrana"));
            info.setLowsusranb(TextUtils.isEmpty(keyValueMap.get("lowsusranb")) ? "" : keyValueMap.get("lowsusranb"));
            info.setUptbtss(TextUtils.isEmpty(keyValueMap.get("uptbtss")) ? "" : keyValueMap.get("uptbtss"));
            info.setUptorblothr(TextUtils.isEmpty(keyValueMap.get("uptorblothr")) ? "" : keyValueMap.get("uptorblothr"));
            info.setUptordetime(TextUtils.isEmpty(keyValueMap.get("uptordetime")) ? "" : keyValueMap.get("uptordetime"));
            info.setUpsusrana(TextUtils.isEmpty(keyValueMap.get("upsusrana")) ? "" : keyValueMap.get("upsusrana"));
            info.setUpsusranb(TextUtils.isEmpty(keyValueMap.get("upsusranb")) ? "" : keyValueMap.get("upsusranb"));

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
