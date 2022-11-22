package com.shmedo.configlibrary.iot.cmd.parser.vms;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalCommInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/2/20 <br/>
 * 描述：    解析Vms终端通信参数
 */
public class VmsTerminalCommInfoParser implements IOTResultParser<VmsTerminalCommInfo> {
    @Override
    public VmsTerminalCommInfo parse(String result) {
        VmsTerminalCommInfo info = new VmsTerminalCommInfo();
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
            info.setNetid(keyValueMap.getOrDefault("netid", "NullKey"));
            info.setDstaddr(keyValueMap.getOrDefault("dstaddr", "NullKey"));
            info.setChannel(keyValueMap.getOrDefault("channel", "NullKey"));
            info.setAirbaud(keyValueMap.getOrDefault("airbaud", "NullKey"));

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
        return IOTCommandType.VMS_MD_GET_TERMINAL_COMMUNICATE;
    }
}
