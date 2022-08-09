package com.shmedo.configlibrary.iot.cmd.parser.e40;

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
            info.setSw(keyValueMap.getOrDefault("sw", "NullKey"));
            info.setAddr(keyValueMap.getOrDefault("addr", "NullKey"));
            info.setPort(keyValueMap.getOrDefault("port", "NullKey"));
            info.setUser(keyValueMap.getOrDefault("user", "NullKey"));
            info.setPswd(keyValueMap.getOrDefault("pswd", "NullKey"));
            info.setSta(keyValueMap.getOrDefault("sta", "NullKey"));

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
