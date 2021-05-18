package com.shmedo.configlibrary.iot.cmd.parser.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.e40.E40SerialPortInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/18 <br/>
 * 描述：       解析串口参数
 */
public class E40SerialPortInfoParser implements IOTResultParser<E40SerialPortInfo> {
    @Override
    public E40SerialPortInfo parse(String result) {
        E40SerialPortInfo info = new E40SerialPortInfo();
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
            info.setType(TextUtils.isEmpty(keyValueMap.get("type")) ? "" : keyValueMap.get("type"));
            info.setBaud(TextUtils.isEmpty(keyValueMap.get("baud")) ? "" : keyValueMap.get("baud"));
            info.setDatabits(TextUtils.isEmpty(keyValueMap.get("databits")) ? "" : keyValueMap.get("databits"));
            info.setParity(TextUtils.isEmpty(keyValueMap.get("parity")) ? "" : keyValueMap.get("parity"));
            info.setStopbits(TextUtils.isEmpty(keyValueMap.get("stopbits")) ? "" : keyValueMap.get("stopbits"));

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
        return IOTCommandType.E40_MD_GET_DB_GUART;
    }
}
