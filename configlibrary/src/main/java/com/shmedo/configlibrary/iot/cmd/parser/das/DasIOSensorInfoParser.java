package com.shmedo.configlibrary.iot.cmd.parser.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasIOSensorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/19 <br/>
 * 描述：      解析DAS 开关量传感器参数
 */
public class DasIOSensorInfoParser implements IOTResultParser<DasIOSensorInfo> {
    @Override
    public DasIOSensorInfo parse(String result) {
        DasIOSensorInfo info = new DasIOSensorInfo();
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
            info.setValue(TextUtils.isEmpty(keyValueMap.get("value")) ? "" : keyValueMap.get("value"));

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
        return IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO;
    }
}
