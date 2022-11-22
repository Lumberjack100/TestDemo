package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicConfigInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/24/20 <br/>
 * 描述：     解析ADME基础配置参数
 */
public class AdmeBasicConfigParamParser implements IOTResultParser<AdmeBasicConfigInfo> {
    @Override
    public AdmeBasicConfigInfo parse(String result) {
        AdmeBasicConfigInfo info = new AdmeBasicConfigInfo();
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
            info.setAddress(keyValueMap.getOrDefault("address", "NullKey"));
            info.setInterdeep(keyValueMap.getOrDefault("interdeep", "NullKey"));
            info.setDownspeed(keyValueMap.getOrDefault("downspeed", "NullKey"));
            info.setDownwaitetime(keyValueMap.getOrDefault("downwaitetime", "NullKey"));
            info.setDatatype(keyValueMap.getOrDefault("datatype", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_BASIC;
    }
}
