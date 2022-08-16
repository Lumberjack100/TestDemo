package com.shmedo.configlibrary.iot.cmd.parser;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.DataCenterStatus;

import java.util.HashMap;
import java.util.Objects;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/29/20 <br/>
 * 描述：      解析数据中心状态
 */
public class DataCenterStatusParser implements IOTResultParser<DataCenterStatus> {
    @Override
    public DataCenterStatus parse(String result) {
        DataCenterStatus info = new DataCenterStatus();
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
            info.setCenterid(Integer.parseInt(Objects.requireNonNull(keyValueMap.get("centerid"))));
            info.setStatus(keyValueMap.getOrDefault("status", "NullKey"));

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
        return IOTCommandType.MD_GET_DATA_CENTER_STATUS;
    }
}
