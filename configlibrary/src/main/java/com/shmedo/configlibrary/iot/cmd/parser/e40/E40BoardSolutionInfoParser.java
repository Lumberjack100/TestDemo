package com.shmedo.configlibrary.iot.cmd.parser.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.e40.E40BoardSolutionInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：    解析板卡解算参数
 */
public class E40BoardSolutionInfoParser implements IOTResultParser<E40BoardSolutionInfo> {
    @Override
    public E40BoardSolutionInfo parse(String result) {
        E40BoardSolutionInfo info = new E40BoardSolutionInfo();
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
            info.setInittime(TextUtils.isEmpty(keyValueMap.get("inittime")) ? "" : keyValueMap.get("inittime"));
            info.setCalcgap(TextUtils.isEmpty(keyValueMap.get("calcgap")) ? "" : keyValueMap.get("calcgap"));
            info.setSmoothlevel(TextUtils.isEmpty(keyValueMap.get("smoothlevel")) ? "" : keyValueMap.get("smoothlevel"));
            info.setReinit(TextUtils.isEmpty(keyValueMap.get("reinit")) ? "" : keyValueMap.get("reinit"));
            info.setRtkdynamicmode(TextUtils.isEmpty(keyValueMap.get("rtkdynamicmode")) ? "" : keyValueMap.get("rtkdynamicmode"));
            info.setCorrval(TextUtils.isEmpty(keyValueMap.get("corrval")) ? "" : keyValueMap.get("corrval"));

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
        return IOTCommandType.E40_MD_GET_BOARDSOLUTION;
    }
}
