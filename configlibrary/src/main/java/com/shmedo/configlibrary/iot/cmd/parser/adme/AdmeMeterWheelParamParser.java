package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeterWheelInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/27/20 <br/>
 * 描述：      解析ADME计米轮配置参数
 */
public class AdmeMeterWheelParamParser implements IOTResultParser<AdmeMeterWheelInfo> {
    @Override
    public AdmeMeterWheelInfo parse(String result) {
        AdmeMeterWheelInfo info = new AdmeMeterWheelInfo();
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
            info.setEnclinenum(keyValueMap.getOrDefault("enclinenum", "NullKey"));
            info.setOutline(keyValueMap.getOrDefault("outline", "NullKey"));
            info.setUptiona(keyValueMap.getOrDefault("uptiona", "NullKey"));
            info.setUptionb(keyValueMap.getOrDefault("uptionb", "NullKey"));
            info.setUpconstant(keyValueMap.getOrDefault("upconstant", "NullKey"));
            info.setUpfilter(keyValueMap.getOrDefault("upfilter", "NullKey"));
            info.setDowntiona(keyValueMap.getOrDefault("downtiona", "NullKey"));
            info.setDowntionb(keyValueMap.getOrDefault("downtionb", "NullKey"));
            info.setDownconstant(keyValueMap.getOrDefault("downconstant", "NullKey"));
            info.setDownfilter(keyValueMap.getOrDefault("downfilter", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_METER_WHEEL;
    }
}
