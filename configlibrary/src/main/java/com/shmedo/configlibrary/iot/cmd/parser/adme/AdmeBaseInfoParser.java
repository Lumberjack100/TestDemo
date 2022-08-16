package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeBaseInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/23/20 <br/>
 * 描述：      解析ADME基础信息
 */
public class AdmeBaseInfoParser implements IOTResultParser<AdmeBaseInfo> {
    @Override
    public AdmeBaseInfo parse(String result) {
        AdmeBaseInfo info = new AdmeBaseInfo();
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
            info.setEquimodel(keyValueMap.getOrDefault("equimodel", "NullKey"));
            info.setOnline(keyValueMap.getOrDefault("online", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS;
    }
}
