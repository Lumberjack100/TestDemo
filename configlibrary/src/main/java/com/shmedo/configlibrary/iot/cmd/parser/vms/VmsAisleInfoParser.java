package com.shmedo.configlibrary.iot.cmd.parser.vms;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;

import java.util.HashMap;
import java.util.Objects;

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
            info.setChannel(Integer.parseInt(Objects.requireNonNull(keyValueMap.get("channel"))));
            info.setNetid(keyValueMap.getOrDefault("sn", "NullKey"));
            info.setAddr(keyValueMap.getOrDefault("sn", "NullKey"));
            info.setChl(keyValueMap.getOrDefault("sn", "NullKey"));
            info.setAirbaud(keyValueMap.getOrDefault("airbaud", "NullKey"));
            info.setPpt(keyValueMap.getOrDefault("ppt", "NullKey"));
            info.setTerminalmode(keyValueMap.getOrDefault("terminalmode", "NullKey"));
            info.setSendgap(keyValueMap.getOrDefault("sendgap", "NullKey"));
            info.setOffline(keyValueMap.getOrDefault("offline", "NullKey"));
            info.setSleepgap(keyValueMap.getOrDefault("sleepgap", "NullKey"));
            info.setWakeupgap(keyValueMap.getOrDefault("wakeupgap", "NullKey"));
            info.setTerminalnum(keyValueMap.getOrDefault("terminalnum", "NullKey"));
            info.setRssi(keyValueMap.getOrDefault("rssi", "NullKey"));

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
