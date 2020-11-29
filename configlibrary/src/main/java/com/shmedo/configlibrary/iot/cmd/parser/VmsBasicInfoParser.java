package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.VmsBasicInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/12 <br/>
 * 描述：   解析Vms网关基础信息
 */
public class VmsBasicInfoParser implements IOTResultParser<VmsBasicInfo> {
    @Override
    public VmsBasicInfo parse(String result) {
        VmsBasicInfo info = new VmsBasicInfo();
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
            info.setOnline(TextUtils.isEmpty(keyValueMap.get("online")) ? "" : keyValueMap.get("online"));
            info.setSwVersion(TextUtils.isEmpty(keyValueMap.get("sw")) ? "" : keyValueMap.get("sw"));
            info.setVolt(TextUtils.isEmpty(keyValueMap.get("volt")) ? "" : keyValueMap.get("volt"));

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
        return IOTCommandType.VMS_MD_GET_GATEWAY_BASE;
    }
}
