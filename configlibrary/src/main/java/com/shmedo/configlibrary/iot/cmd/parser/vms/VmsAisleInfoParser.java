package com.shmedo.configlibrary.iot.cmd.parser.vms;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/15/20 <br/>
 * 描述：    解析Vms网关通道参数
 */
public class VmsAisleInfoParser implements IOTResultParser<VmsAisleInfo> {
    @Override
    public VmsAisleInfo parse(String result) {
        VmsAisleInfo info = new VmsAisleInfo();
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
            info.setChannel(Integer.parseInt(keyValueMap.get("channel")));
            info.setNetid(!keyValueMap.containsKey("netid") ? "NullKey" :  keyValueMap.get("netid"));
            info.setAddr(!keyValueMap.containsKey("addr") ? "NullKey" : keyValueMap.get("addr"));
            info.setChl(!keyValueMap.containsKey("chl") ? "NullKey": keyValueMap.get("chl"));
            info.setAirbaud(!keyValueMap.containsKey("airbaud") ? "NullKey" : keyValueMap.get("airbaud"));
            info.setPpt(!keyValueMap.containsKey("ppt") ? "NullKey" : keyValueMap.get("ppt"));
            info.setTerminalmode(!keyValueMap.containsKey("terminalmode") ? "NullKey": keyValueMap.get("terminalmode"));
            info.setSendgap(!keyValueMap.containsKey("sendgap") ? "NullKey" : keyValueMap.get("sendgap"));
            info.setOffline(!keyValueMap.containsKey("offline") ? "NullKey": keyValueMap.get("offline"));
            info.setSleepgap(!keyValueMap.containsKey("sleepgap") ? "NullKey" : keyValueMap.get("sleepgap"));
            info.setWakeupgap(!keyValueMap.containsKey("wakeupgap") ? "NullKey" : keyValueMap.get("wakeupgap"));
            info.setTerminalnum(!keyValueMap.containsKey("terminalnum") ? "NullKey" : keyValueMap.get("terminalnum"));
            info.setRssi(!keyValueMap.containsKey("rssi") ? "NullKey": keyValueMap.get("rssi"));

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
        return IOTCommandType.VMS_MD_GET_GATEWAY_PARAM;
    }
}
