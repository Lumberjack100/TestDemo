package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeterWheelParam;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/27/20 <br/>
 * 描述：      解析ADME计米轮配置参数
 */
public class AdmeMeterWheelParamParser implements IOTResultParser<AdmeMeterWheelParam> {
    @Override
    public AdmeMeterWheelParam parse(String result) {
        AdmeMeterWheelParam info = new AdmeMeterWheelParam();
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
            info.setEnclinenum(TextUtils.isEmpty(keyValueMap.get("enclinenum")) ? "" : keyValueMap.get("enclinenum"));
            info.setOutline(TextUtils.isEmpty(keyValueMap.get("outline")) ? "" : keyValueMap.get("outline"));
            info.setUptiona(TextUtils.isEmpty(keyValueMap.get("uptiona")) ? "" : keyValueMap.get("uptiona"));
            info.setUptionb(TextUtils.isEmpty(keyValueMap.get("uptionb")) ? "" : keyValueMap.get("uptionb"));
            info.setUpconstant(TextUtils.isEmpty(keyValueMap.get("upconstant")) ? "" : keyValueMap.get("upconstant"));
            info.setUpfilter(TextUtils.isEmpty(keyValueMap.get("upfilter")) ? "" : keyValueMap.get("upfilter"));
            info.setDowntiona(TextUtils.isEmpty(keyValueMap.get("downtiona")) ? "" : keyValueMap.get("downtiona"));
            info.setDowntionb(TextUtils.isEmpty(keyValueMap.get("downtionb")) ? "" : keyValueMap.get("downtionb"));
            info.setDownconstant(TextUtils.isEmpty(keyValueMap.get("downconstant")) ? "" : keyValueMap.get("downconstant"));
            info.setDownfilter(TextUtils.isEmpty(keyValueMap.get("downfilter")) ? "" : keyValueMap.get("downfilter"));

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
        return IOTCommandType.ADME_MD_GET_METER_WHEEL_PARAMETERS;
    }
}
