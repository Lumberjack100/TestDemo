package com.shmedo.configlibrary.iot.cmd.parser.m20;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.m20.M20BaseInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/27/21 <br/>
 * 描述：    解析M20基本信息
 */
public class M20BaseInfoParser implements IOTResultParser<M20BaseInfo> {
    @Override
    public M20BaseInfo parse(String result) {
        M20BaseInfo info = new M20BaseInfo();
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
            info.setSn(keyValueMap.getOrDefault("sn", "NullKey"));
            info.setProductid(keyValueMap.getOrDefault("productid", "NullKey"));
            info.setFirversion(keyValueMap.getOrDefault("firversion", "NullKey"));

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
        return IOTCommandType.M20_MD_GET_BASE_INFO;
    }
}
