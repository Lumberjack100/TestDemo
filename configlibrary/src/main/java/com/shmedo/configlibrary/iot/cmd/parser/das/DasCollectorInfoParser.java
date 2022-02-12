package com.shmedo.configlibrary.iot.cmd.parser.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasCollectorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  4/12/21 <br/>
 * 描述：     解析DAS 采集器参数
 */
public class DasCollectorInfoParser implements IOTResultParser<DasCollectorInfo> {
    @Override
    public DasCollectorInfo parse(String result) {
        DasCollectorInfo info = new DasCollectorInfo();
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
            info.setAddr(TextUtils.isEmpty(keyValueMap.get("addr")) ? "" : keyValueMap.get("addr"));
            info.setCollgap(TextUtils.isEmpty(keyValueMap.get("collgap")) ? "" : keyValueMap.get("collgap"));
            info.setCalcgap(TextUtils.isEmpty(keyValueMap.get("calcgap")) ? "" : keyValueMap.get("calcgap"));
            info.setStandbygap(TextUtils.isEmpty(keyValueMap.get("standbygap")) ? "" : keyValueMap.get("standbygap"));
            info.setSensornum(TextUtils.isEmpty(keyValueMap.get("sensornum")) ? "" : keyValueMap.get("sensornum"));
            info.setSensitivity(keyValueMap.getOrDefault("sensitivity", "NullKey"));

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
        return IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL;
    }
}
