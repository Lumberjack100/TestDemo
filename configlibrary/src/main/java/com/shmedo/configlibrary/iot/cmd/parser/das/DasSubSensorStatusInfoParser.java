package com.shmedo.configlibrary.iot.cmd.parser.das;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasSubSensorStatusInfo;
import com.shmedo.core.util.GsonFactory;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：     TODO
 */
public class DasSubSensorStatusInfoParser implements IOTResultParser<DasSubSensorStatusInfo> {
    @Override
    public DasSubSensorStatusInfo parse(String result) {
        DasSubSensorStatusInfo info = null;
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
            info = GsonFactory.getGson().fromJson(status, DasSubSensorStatusInfo.class);

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
        return IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS;
    }
}
