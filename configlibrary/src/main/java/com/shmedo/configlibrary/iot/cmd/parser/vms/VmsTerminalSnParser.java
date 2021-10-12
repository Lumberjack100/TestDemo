package com.shmedo.configlibrary.iot.cmd.parser.vms;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSn;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/10/12 <br/>
 * 描述：     TODO
 */
public class VmsTerminalSnParser implements IOTResultParser<VmsTerminalSn> {
    @Override
    public VmsTerminalSn parse(String result) {
        VmsTerminalSn info = new VmsTerminalSn();
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
            info.setSn(TextUtils.isEmpty(keyValueMap.get("sn")) ? "" : keyValueMap.get("sn").replace("\"",""));

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
        return IOTCommandType.VMS_MD_GET_TERMINAL_SN;
    }
}
