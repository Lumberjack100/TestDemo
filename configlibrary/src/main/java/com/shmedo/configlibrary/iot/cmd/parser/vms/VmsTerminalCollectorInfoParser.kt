package com.shmedo.configlibrary.iot.cmd.parser.vms;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalCollectorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/2/20 <br/>
 * 描述：    解析Vms终端采集参数
 */
public class VmsTerminalCollectorInfoParser implements IOTResultParser<VmsTerminalCollectorInfo> {
    @Override
    public VmsTerminalCollectorInfo parse(String result) {
        VmsTerminalCollectorInfo info = new VmsTerminalCollectorInfo();
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
            info.setReptgap(keyValueMap.getOrDefault("reptgap", "NullKey"));
            info.setRepttype(keyValueMap.getOrDefault("repttype", "NullKey"));
            info.setFiltertype(keyValueMap.getOrDefault("filtertype", "NullKey"));
            info.setFilternum(keyValueMap.getOrDefault("filternum", "NullKey"));
            info.setCollgap(keyValueMap.getOrDefault("collgap", "NullKey"));
            info.setWaitgap(keyValueMap.getOrDefault("waitgap", "NullKey"));

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
        return IOTCommandType.VMS_MD_GET_TERMINAL_COLLECTOR;
    }
}
