package com.shmedo.configlibrary.iot.cmd.parser.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.e40.E40CORSInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/26/21 <br/>
 * 描述：     解析 CORS 服务参数
 */
public class E40CORSInfoParser implements IOTResultParser<E40CORSInfo> {
    @Override
    public E40CORSInfo parse(String result) {
        E40CORSInfo info = new E40CORSInfo();
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
            info.setSw(TextUtils.isEmpty(keyValueMap.get("sw")) ? "0" : keyValueMap.get("sw"));
            info.setAddr(TextUtils.isEmpty(keyValueMap.get("addr")) ? "" : keyValueMap.get("addr"));
            info.setPort(TextUtils.isEmpty(keyValueMap.get("port")) ? "" : keyValueMap.get("port"));
            info.setUser(TextUtils.isEmpty(keyValueMap.get("user")) ? "" : keyValueMap.get("user"));
            info.setPswd(TextUtils.isEmpty(keyValueMap.get("pswd")) ? "" : keyValueMap.get("pswd"));
            info.setSta(TextUtils.isEmpty(keyValueMap.get("sta")) ? "" : keyValueMap.get("sta"));

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
        return IOTCommandType.E40_MD_GET_CORS;
    }
}
