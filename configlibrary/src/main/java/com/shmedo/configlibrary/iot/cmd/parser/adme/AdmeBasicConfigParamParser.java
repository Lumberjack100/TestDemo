package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

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
            info.setInctype(TextUtils.isEmpty(keyValueMap.get("inctype")) ? "" : keyValueMap.get("inctype"));
            info.setAddress(TextUtils.isEmpty(keyValueMap.get("address")) ? "" : keyValueMap.get("address"));
            info.setInterdeep(TextUtils.isEmpty(keyValueMap.get("interdeep")) ? "" : keyValueMap.get("interdeep"));
            info.setDownspeed(TextUtils.isEmpty(keyValueMap.get("downspeed")) ? "" : keyValueMap.get("downspeed"));
            info.setDownwaitetime(TextUtils.isEmpty(keyValueMap.get("downwaitetime")) ? "" : keyValueMap.get("downwaitetime"));
            info.setDatatype(TextUtils.isEmpty(keyValueMap.get("datatype")) ? "" : keyValueMap.get("datatype"));

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
