package com.shmedo.configlibrary.iot.cmd.parser.das;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasBaseInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：      解析DAS 状态页面基本信息参数
 */
public class DasBaseInfoParser implements IOTResultParser<DasBaseInfo> {
    @Override
    public DasBaseInfo parse(String result) {
        DasBaseInfo info = new DasBaseInfo();
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
            info.setIccid(keyValueMap.getOrDefault("iccid", "NullKey"));
            info.setImei(keyValueMap.getOrDefault("imei", "NullKey"));
            info.setVer(keyValueMap.getOrDefault("ver", "NullKey"));
            info.setLocal(keyValueMap.getOrDefault("local", "NullKey"));
            info.setInvolt(keyValueMap.getOrDefault("involt", "NullKey"));
            info.setOutvolt(keyValueMap.getOrDefault("outvolt", "NullKey"));
            info.setCsq(keyValueMap.getOrDefault("csq", "NullKey"));
            info.setIsp(keyValueMap.getOrDefault("isp", "NullKey"));
            info.setCode(keyValueMap.getOrDefault("code", "NullKey"));

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
        return IOTCommandType.DAS_MD_GET_DEVICE_BASE;
    }
}
