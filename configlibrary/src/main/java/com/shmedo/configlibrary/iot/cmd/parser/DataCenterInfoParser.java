package com.shmedo.configlibrary.iot.cmd.parser;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/18/20 <br/>
 * 描述：   解析数据中心参数
 */
public class DataCenterInfoParser implements IOTResultParser<DataCenterInfo> {
    @Override
    public DataCenterInfo parse(String result) {
        DataCenterInfo info = new DataCenterInfo();
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
            info.setCenterid(keyValueMap.getOrDefault("centerid", "NullKey"));
            info.setProtocol(keyValueMap.getOrDefault("protocol", "NullKey"));
            info.setDatatype(keyValueMap.getOrDefault("datatype", "NullKey"));
            info.setPlattype(keyValueMap.getOrDefault("plattype", "NullKey"));
            info.setAddr(keyValueMap.getOrDefault("addr", "NullKey"));
            info.setPort(keyValueMap.getOrDefault("port", "NullKey"));
            info.setDeviceid(keyValueMap.getOrDefault("deviceid", "NullKey"));
            info.setDevicekey(keyValueMap.getOrDefault("devicekey", "NullKey"));
            info.setHttpaddr(keyValueMap.getOrDefault("httpaddr", "NullKey"));
            info.setHttpport(keyValueMap.getOrDefault("httpport", "NullKey"));
            info.setProjid(keyValueMap.getOrDefault("projid", "NullKey"));
            info.setRegcode(keyValueMap.getOrDefault("regcode", "NullKey"));

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
        return IOTCommandType.MD_GET_DATA_CENTER;
    }
}
