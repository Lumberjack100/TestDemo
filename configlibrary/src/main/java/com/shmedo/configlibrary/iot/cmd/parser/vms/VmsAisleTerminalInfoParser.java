package com.shmedo.configlibrary.iot.cmd.parser.vms;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleTerminalInfo;
import com.shmedo.core.util.GsonFactory;

import java.util.HashMap;


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：     TODO
 */
public class VmsAisleTerminalInfoParser implements IOTResultParser<VmsAisleTerminalInfo> {
    @Override
    public VmsAisleTerminalInfo parse(String result) {
        VmsAisleTerminalInfo info;
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

            String status = keyValueMap.get("status");
            info = GsonFactory.getGson().fromJson(status, VmsAisleTerminalInfo.class);
            info.setChannel(Integer.parseInt(keyValueMap.get("channel")));

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
        return IOTCommandType.VMS_MD_GET_GATEWAY_STATUS;
    }
}
