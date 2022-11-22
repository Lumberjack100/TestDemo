package com.shmedo.configlibrary.iot.cmd.parser.vms;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/23/20 <br/>
 * 描述：     解析Vms终端传感器数据
 */
public class VmsTerminalSensorInfoParser implements IOTResultParser<VmsTerminalSensorInfo> {
    @Override
    public VmsTerminalSensorInfo parse(String result) {
        VmsTerminalSensorInfo info = new VmsTerminalSensorInfo();
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
            info.setSn(keyValueMap.getOrDefault("sn", "NullKey"));
            info.setChannel(keyValueMap.getOrDefault("channel", "NullKey"));
            info.setInsert(keyValueMap.getOrDefault("insert", "NullKey"));
            info.setFreqtype(keyValueMap.getOrDefault("freqtype", "NullKey"));
            info.setFreqmax(keyValueMap.getOrDefault("freqmax", "NullKey"));
            info.setFreqmin(keyValueMap.getOrDefault("freqmin", "NullKey"));
            info.setVolttype(keyValueMap.getOrDefault("volttype", "NullKey"));
            info.setExpvolt(keyValueMap.getOrDefault("expvolt", "NullKey"));
            info.setType(keyValueMap.getOrDefault("type", "NullKey"));
            info.setName(keyValueMap.getOrDefault("name", "NullKey"));
            info.setGateval(keyValueMap.getOrDefault("gateval", "NullKey"));
            info.setCorral(keyValueMap.getOrDefault("corral", "NullKey"));
            info.setFixsite(keyValueMap.getOrDefault("fixsite", "NullKey"));
            info.setRopelen(keyValueMap.getOrDefault("ropelen", "NullKey"));
            info.setParama(keyValueMap.getOrDefault("parama", "NullKey"));
            info.setParamb(keyValueMap.getOrDefault("paramb", "NullKey"));
            info.setParamc(keyValueMap.getOrDefault("paramc", "NullKey"));
            info.setParamk(keyValueMap.getOrDefault("paramk", "NullKey"));
            info.setParamm(keyValueMap.getOrDefault("paramm", "NullKey"));
            info.setParamf(keyValueMap.getOrDefault("paramf", "NullKey"));
            info.setParamt(keyValueMap.getOrDefault("paramt", "NullKey"));


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
        return IOTCommandType.VMS_MD_GET_TERMINAL_CHL;
    }
}
