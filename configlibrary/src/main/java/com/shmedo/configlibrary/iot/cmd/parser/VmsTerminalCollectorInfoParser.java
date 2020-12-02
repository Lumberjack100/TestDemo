package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.VmsTerminalCollectorInfo;

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
            info.setReptgap(TextUtils.isEmpty(keyValueMap.get("reptgap")) ? "" : keyValueMap.get("reptgap"));
            info.setRepttype(TextUtils.isEmpty(keyValueMap.get("repttype")) ? "" : keyValueMap.get("repttype"));
            info.setFiltertype(TextUtils.isEmpty(keyValueMap.get("filtertype")) ? "" : keyValueMap.get("filtertype"));
            info.setFilternum(TextUtils.isEmpty(keyValueMap.get("filternum")) ? "" : keyValueMap.get("filternum"));
            info.setCollgap(TextUtils.isEmpty(keyValueMap.get("collgap")) ? "" : keyValueMap.get("collgap"));
            info.setWaitgap(TextUtils.isEmpty(keyValueMap.get("waitgap")) ? "" : keyValueMap.get("waitgap"));

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
