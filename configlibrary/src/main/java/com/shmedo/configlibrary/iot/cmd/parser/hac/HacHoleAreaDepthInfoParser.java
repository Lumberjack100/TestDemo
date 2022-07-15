package com.shmedo.configlibrary.iot.cmd.parser.hac;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.hac.HacHoleAreaDepthInfo;

import java.util.HashMap;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/12 <br/>
 * 描述：      解析 AC10 孔深测量参数
 */
public class HacHoleAreaDepthInfoParser implements IOTResultParser<List<HacHoleAreaDepthInfo>> {
    @Override
    public List<HacHoleAreaDepthInfo> parse(String result) {
        List<HacHoleAreaDepthInfo> holeAreaDepthInfoListList = null;
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
            String value = keyValueMap.get("holelist");
            holeAreaDepthInfoListList = TextUtils.isEmpty(value) ? null : GsonUtils.fromJson(value, new TypeToken<List<HacHoleAreaDepthInfo>>() {
            }.getType());

            return holeAreaDepthInfoListList;
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
        return IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM;
    }
}
