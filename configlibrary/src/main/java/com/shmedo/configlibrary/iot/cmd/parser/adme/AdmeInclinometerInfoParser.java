package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

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
            info.setInctype(TextUtils.isEmpty(keyValueMap.get("inctype")) ? "" : keyValueMap.get("inctype"));
            info.setLowpower(TextUtils.isEmpty(keyValueMap.get("lowpower")) ? "" : keyValueMap.get("lowpower"));
            info.setAddress(TextUtils.isEmpty(keyValueMap.get("address")) ? "" : keyValueMap.get("address"));
            info.setCollinval(TextUtils.isEmpty(keyValueMap.get("collinval")) ? "" : keyValueMap.get("collinval"));
            info.setCalcinval(TextUtils.isEmpty(keyValueMap.get("calcinval")) ? "" : keyValueMap.get("calcinval"));
            info.setDormancytime(TextUtils.isEmpty(keyValueMap.get("dormancytime")) ? "" : keyValueMap.get("dormancytime"));
            info.setInterupdate(TextUtils.isEmpty(keyValueMap.get("interupdate")) ? "" : keyValueMap.get("interupdate"));

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
