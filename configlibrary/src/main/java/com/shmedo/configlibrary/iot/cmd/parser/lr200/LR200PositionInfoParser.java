package com.shmedo.configlibrary.iot.cmd.parser.lr200;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.lr200.LR200PositionInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/25 <br/>
 * 描述：     解析一体式裂缝计经纬度信息
 */
public class LR200PositionInfoParser implements IOTResultParser<LR200PositionInfo> {
    @Override
    public LR200PositionInfo parse(String result) {
        LR200PositionInfo info = new LR200PositionInfo();
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
            info.setLng(TextUtils.isEmpty(keyValueMap.get("lng")) ? "" : keyValueMap.get("lng"));
            info.setLat(TextUtils.isEmpty(keyValueMap.get("lat")) ? "" : keyValueMap.get("lat"));

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
        return IOTCommandType.MD_GET_LOCATION;
    }
}
