package com.shmedo.configlibrary.iot.cmd.parser.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasDigitalPiezometerInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/19 <br/>
 * 描述：      解析DAS 数字水位计参数
 */
public class DasDigitalPiezometerInfoParser implements IOTResultParser<DasDigitalPiezometerInfo> {
    @Override
    public DasDigitalPiezometerInfo parse(String result) {
        DasDigitalPiezometerInfo info = new DasDigitalPiezometerInfo();
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
            info.setAddr(TextUtils.isEmpty(keyValueMap.get("addr")) ? "" : keyValueMap.get("addr"));
            info.setSw(TextUtils.isEmpty(keyValueMap.get("sw")) ? "" : keyValueMap.get("sw"));
            info.setThreshold(TextUtils.isEmpty(keyValueMap.get("threshold")) ? "" : keyValueMap.get("threshold"));
            info.setCorrval(TextUtils.isEmpty(keyValueMap.get("corrval")) ? "" : keyValueMap.get("corrval"));
            info.setRopelen(TextUtils.isEmpty(keyValueMap.get("ropelen")) ? "" : keyValueMap.get("ropelen"));
            info.setTubealti(TextUtils.isEmpty(keyValueMap.get("tubealti")) ? "" : keyValueMap.get("tubealti"));

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
        return IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO;
    }
}
