package com.shmedo.configlibrary.iot.cmd.parser.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/21 <br/>
 * 描述：      解析DAS扩展传感器参数
 */
public class DasExternalSensorInfoParser implements IOTResultParser<DasExternalSensorInfo> {
    @Override
    public DasExternalSensorInfo parse(String result) {
        DasExternalSensorInfo info = new DasExternalSensorInfo();
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
            info.setIndex(TextUtils.isEmpty(keyValueMap.get("index")) ? "" : keyValueMap.get("index"));
            info.setType(TextUtils.isEmpty(keyValueMap.get("type")) ? "" : keyValueMap.get("type"));
            info.setAddr(TextUtils.isEmpty(keyValueMap.get("addr")) ? "" : keyValueMap.get("addr"));
            info.setThreshold(TextUtils.isEmpty(keyValueMap.get("threshold")) ? "" : keyValueMap.get("threshold"));
            info.setCorrval(TextUtils.isEmpty(keyValueMap.get("corrval")) ? "" : keyValueMap.get("corrval"));
            info.setSpacing(TextUtils.isEmpty(keyValueMap.get("spacing")) ? "" : keyValueMap.get("spacing"));
            info.setHolenum(TextUtils.isEmpty(keyValueMap.get("holenum")) ? "" : keyValueMap.get("holenum"));
            info.setTubealti(TextUtils.isEmpty(keyValueMap.get("tubealti")) ? "" : keyValueMap.get("tubealti"));
            info.setRopelen(TextUtils.isEmpty(keyValueMap.get("ropelen")) ? "" : keyValueMap.get("ropelen"));
            info.setPoly_a(TextUtils.isEmpty(keyValueMap.get("poly_a")) ? "" : keyValueMap.get("poly_a"));
            info.setPoly_b(TextUtils.isEmpty(keyValueMap.get("poly_b")) ? "" : keyValueMap.get("poly_b"));
            info.setPoly_c(TextUtils.isEmpty(keyValueMap.get("poly_c")) ? "" : keyValueMap.get("poly_c"));
            info.setTemp_k(TextUtils.isEmpty(keyValueMap.get("temp_k")) ? "" : keyValueMap.get("temp_k"));
            info.setTemp_t0(TextUtils.isEmpty(keyValueMap.get("temp_t0")) ? "" : keyValueMap.get("temp_t0"));
            info.setSens_k(TextUtils.isEmpty(keyValueMap.get("sens_k")) ? "" : keyValueMap.get("sens_k"));
            info.setTemp_b(TextUtils.isEmpty(keyValueMap.get("temp_b")) ? "" : keyValueMap.get("temp_b"));
            info.setReferval_f(TextUtils.isEmpty(keyValueMap.get("referval_f")) ? "" : keyValueMap.get("referval_f"));
            info.setElastic_mod(TextUtils.isEmpty(keyValueMap.get("elastic_mod")) ? "" : keyValueMap.get("elastic_mod"));

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
        return IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR;
    }
}
