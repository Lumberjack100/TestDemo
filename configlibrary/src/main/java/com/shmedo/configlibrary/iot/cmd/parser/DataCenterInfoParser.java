package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

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
            info.setProtocol(TextUtils.isEmpty(keyValueMap.get("protocol")) ? "" : keyValueMap.get("protocol"));
            info.setDatatype(TextUtils.isEmpty(keyValueMap.get("datatype")) ? "" : keyValueMap.get("datatype"));
            info.setAddr(TextUtils.isEmpty(keyValueMap.get("addr")) ? "" : keyValueMap.get("addr"));
            info.setPort(TextUtils.isEmpty(keyValueMap.get("port")) ? "" : keyValueMap.get("port"));
            info.setDeviceid(TextUtils.isEmpty(keyValueMap.get("deviceid")) ? "" : keyValueMap.get("deviceid"));
            info.setDevicekey(TextUtils.isEmpty(keyValueMap.get("devicekey")) ? "" : keyValueMap.get("devicekey"));
            info.setHttpaddr(TextUtils.isEmpty(keyValueMap.get("httpaddr")) ? "" : keyValueMap.get("httpaddr"));
            info.setHttpport(TextUtils.isEmpty(keyValueMap.get("httpport")) ? "" : keyValueMap.get("httpport"));
            info.setProjid(TextUtils.isEmpty(keyValueMap.get("projid")) ? "" : keyValueMap.get("projid"));
            info.setRegcode(TextUtils.isEmpty(keyValueMap.get("regcode")) ? "" : keyValueMap.get("regcode"));

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
