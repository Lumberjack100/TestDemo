package com.shmedo.configlibrary.iot.cmd.parser.das;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasSensorStatusInfo;

import java.util.HashMap;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：     解析DAS 状态页面主传感器状态参数
 */
public class DasSensorStatusInfoParser implements IOTResultParser<List<DasSensorStatusInfo>> {
    @Override
    public List<DasSensorStatusInfo> parse(String result) {
        List<DasSensorStatusInfo> sensorStatusInfoList = null;
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
            String status = keyValueMap.get("status");
            sensorStatusInfoList = GsonUtils.fromJson(status, new TypeToken<List<DasSensorStatusInfo>>() {
            }.getType());

            return sensorStatusInfoList;
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
        return IOTCommandType.DAS_MD_GET_SENSOR_STATUS;
    }
}
