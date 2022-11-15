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
            info.setCenterid(keyValueMap.getOrDefault("centerid", ""));
            info.setProtocol(keyValueMap.getOrDefault("protocol", ""));
            info.setDatatype(keyValueMap.getOrDefault("datatype", ""));
            info.setPlattype(keyValueMap.getOrDefault("plattype", ""));
            info.setAddr(keyValueMap.getOrDefault("addr", ""));
            info.setPort(keyValueMap.getOrDefault("port", ""));

            info.setDeviceid(keyValueMap.getOrDefault("deviceid", ""));
            info.setDevicekey(keyValueMap.getOrDefault("devicekey", ""));
            info.setHttpaddr(keyValueMap.getOrDefault("httpaddr", ""));
            info.setHttpport(keyValueMap.getOrDefault("httpport", ""));
            info.setProjid(keyValueMap.getOrDefault("projid", ""));
            info.setRegcode(keyValueMap.getOrDefault("regcode", ""));

            info.setType_code(keyValueMap.getOrDefault("type_code", ""));
            info.setCo_address(keyValueMap.getOrDefault("co_address", ""));
            info.setPassword(keyValueMap.getOrDefault("password", ""));
            info.setTaddress(keyValueMap.getOrDefault("taddress", ""));
            info.setHour_report(keyValueMap.getOrDefault("hour_report", ""));
            info.setData_link(keyValueMap.getOrDefault("data_link", ""));

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
