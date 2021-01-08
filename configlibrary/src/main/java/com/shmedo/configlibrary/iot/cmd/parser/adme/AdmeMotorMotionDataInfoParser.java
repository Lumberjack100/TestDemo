package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionDataInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/7/21 <br/>
 * 描述：       解析电机实时运动数据
 */
public class AdmeMotorMotionDataInfoParser implements IOTResultParser<AdmeMotorMotionDataInfo> {
    @Override
    public AdmeMotorMotionDataInfo parse(String result) {
        AdmeMotorMotionDataInfo info = new AdmeMotorMotionDataInfo();
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
            info.setPulsenumber(TextUtils.isEmpty(keyValueMap.get("pulsenumber")) ? "" : keyValueMap.get("pulsenumber"));
            info.setRealmovedistance(TextUtils.isEmpty(keyValueMap.get("realmovedistance")) ? "" : keyValueMap.get("realmovedistance"));
            info.setRealmoveangle(TextUtils.isEmpty(keyValueMap.get("realmoveangle")) ? "" : keyValueMap.get("realmoveangle"));

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
        return IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE;
    }
}
