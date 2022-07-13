package com.shmedo.configlibrary.iot.cmd.parser.ac10;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.ac10.AdmeAC10HoleAreaDepthInfo;
import com.shmedo.configlibrary.iot.model.ac10.AdmeAC10MeasuringDataInfo;

import java.util.HashMap;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/11 <br/>
 * 描述：    解析 AC10 数据测量配置参数
 */
public class AdmeAC10MeasuringDataInfoParser implements IOTResultParser<AdmeAC10MeasuringDataInfo> {
    @Override
    public AdmeAC10MeasuringDataInfo parse(String result) {
        AdmeAC10MeasuringDataInfo info = new AdmeAC10MeasuringDataInfo();
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
            info.setAddress(TextUtils.isEmpty(keyValueMap.get("address")) ? "" : keyValueMap.get("address"));
            info.setDownwaitetime(TextUtils.isEmpty(keyValueMap.get("downwaitetime")) ? "" : keyValueMap.get("downwaitetime"));
            info.setDatatype(TextUtils.isEmpty(keyValueMap.get("datatype")) ? "" : keyValueMap.get("datatype"));
            info.setOnewaytest(TextUtils.isEmpty(keyValueMap.get("onewaytest")) ? "" : keyValueMap.get("onewaytest"));

            String value = keyValueMap.get("holelist");
            List<AdmeAC10HoleAreaDepthInfo> tempList = TextUtils.isEmpty(value) ? null : GsonUtils.fromJson(value, new TypeToken<List<AdmeAC10HoleAreaDepthInfo>>() {
            }.getType());

            info.setHolelist(tempList);

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
        return IOTCommandType.ADME_AC10_GET_DATA_MEASURE_PARAM;
    }
}
