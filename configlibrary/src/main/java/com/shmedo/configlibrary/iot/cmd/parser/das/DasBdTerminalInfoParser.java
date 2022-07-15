package com.shmedo.configlibrary.iot.cmd.parser.das;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasBdTerminalInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/20 <br/>
 * 描述：     解析DAS  北斗数传终端参数
 */
public class DasBdTerminalInfoParser implements IOTResultParser<DasBdTerminalInfo> {
    @Override
    public DasBdTerminalInfo parse(String result) {
        DasBdTerminalInfo info = new DasBdTerminalInfo();
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
            info.setSw(keyValueMap.getOrDefault("sw", "NullKey"));
            info.setDstaddr(keyValueMap.getOrDefault("dstaddr", "NullKey"));
            info.setBaud(keyValueMap.getOrDefault("baud", "NullKey"));

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
        return IOTCommandType.DAS_MD_GET_BD_TERMINAL;
    }
}
