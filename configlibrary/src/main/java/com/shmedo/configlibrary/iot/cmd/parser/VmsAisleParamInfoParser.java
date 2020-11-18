package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.VmsAisleParamInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/15/20 <br/>
 * 描述：    解析Vms网关通道参数
 */
public class VmsAisleParamInfoParser implements IOTResultParser<VmsAisleParamInfo> {
    @Override
    public VmsAisleParamInfo parse(String result) {
        VmsAisleParamInfo info = new VmsAisleParamInfo();
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
            info.setNetid(TextUtils.isEmpty(keyValueMap.get("netid")) ? "" : keyValueMap.get("netid"));
            info.setPpt(TextUtils.isEmpty(keyValueMap.get("ppt")) ? "" : keyValueMap.get("ppt"));
            info.setAddr(TextUtils.isEmpty(keyValueMap.get("addr")) ? "" : keyValueMap.get("addr"));
            info.setChl(TextUtils.isEmpty(keyValueMap.get("chl")) ? "" : keyValueMap.get("chl"));
            info.setTerminalmode(TextUtils.isEmpty(keyValueMap.get("terminalmode")) ? "" : keyValueMap.get("terminalmode"));
            info.setSendgap(TextUtils.isEmpty(keyValueMap.get("sendgap")) ? "" : keyValueMap.get("sendgap"));
            info.setOffline(TextUtils.isEmpty(keyValueMap.get("offline")) ? "" : keyValueMap.get("offline"));
            info.setSleepgap(TextUtils.isEmpty(keyValueMap.get("sleepgap")) ? "" : keyValueMap.get("sleepgap"));
            info.setWakeupgap(TextUtils.isEmpty(keyValueMap.get("wakeupgap")) ? "" : keyValueMap.get("wakeupgap"));
            info.setAirbaud(TextUtils.isEmpty(keyValueMap.get("airbaud")) ? "" : keyValueMap.get("airbaud"));

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
        return IOTCommandType.MD_GET_GATEWAY_PARAM;
    }
}
