package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/23/20 <br/>
 * 描述：      解析ADME基础信息
 */
public class AdmeBasicInfoParser implements IOTResultParser<AdmeBasicInfo> {
    @Override
    public AdmeBasicInfo parse(String result) {
        AdmeBasicInfo info = new AdmeBasicInfo();
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
            info.setSn(TextUtils.isEmpty(keyValueMap.get("sn")) ? "" : keyValueMap.get("sn"));
            info.setProductid(TextUtils.isEmpty(keyValueMap.get("productid")) ? "" : keyValueMap.get("productid"));
            info.setEquimodel(TextUtils.isEmpty(keyValueMap.get("equimodel")) ? "" : keyValueMap.get("equimodel"));

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
