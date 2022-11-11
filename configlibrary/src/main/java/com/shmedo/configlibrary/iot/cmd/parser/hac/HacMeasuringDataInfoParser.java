package com.shmedo.configlibrary.iot.cmd.parser.hac;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.hac.HacHoleAreaDepthInfo;
import com.shmedo.configlibrary.iot.model.hac.HacMeasuringDataInfo;

import java.util.HashMap;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/11 <br/>
 * 描述：    解析 AC10 数据测量配置参数
 */
public class HacMeasuringDataInfoParser implements IOTResultParser<HacMeasuringDataInfo> {
    @Override
    public HacMeasuringDataInfo parse(String result) {
        HacMeasuringDataInfo info = new HacMeasuringDataInfo();
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
            info.setEquipmodel(keyValueMap.getOrDefault("equipmodel", "NullKey"));
            info.setAddress(keyValueMap.getOrDefault("address", "NullKey"));
            info.setDownwaitetime(keyValueMap.getOrDefault("downwaitetime", "NullKey"));
            info.setDatatype(keyValueMap.getOrDefault("datatype", "NullKey"));
            info.setOnewaytest(keyValueMap.getOrDefault("onewaytest", "NullKey"));
            info.setCheckreverse(keyValueMap.getOrDefault("checkreverse", "NullKey"));

            String value = keyValueMap.get("holelist");
            List<HacHoleAreaDepthInfo> tempList = TextUtils.isEmpty(value) ? null : GsonUtils.fromJson(value, new TypeToken<List<HacHoleAreaDepthInfo>>() {
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
        return IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM;
    }
}
