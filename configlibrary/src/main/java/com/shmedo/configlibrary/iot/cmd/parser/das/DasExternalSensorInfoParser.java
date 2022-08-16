package com.shmedo.configlibrary.iot.cmd.parser.das;

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
            info.setIndex(keyValueMap.getOrDefault("index", "NullKey"));
            info.setType(keyValueMap.getOrDefault("type", "NullKey"));
            info.setAddr(keyValueMap.getOrDefault("addr", "NullKey"));
            info.setThreshold(keyValueMap.getOrDefault("threshold", "NullKey"));
            info.setCorrval(keyValueMap.getOrDefault("corrval", "NullKey"));
            info.setSpacing(keyValueMap.getOrDefault("spacing", "NullKey"));
            info.setHolenum(keyValueMap.getOrDefault("holenum", "NullKey"));
            info.setTubealti(keyValueMap.getOrDefault("tubealti", "NullKey"));
            info.setRopelen(keyValueMap.getOrDefault("ropelen", "NullKey"));
            info.setPoly_a(keyValueMap.getOrDefault("poly_a", "NullKey"));
            info.setPoly_b(keyValueMap.getOrDefault("poly_b", "NullKey"));
            info.setPoly_c(keyValueMap.getOrDefault("poly_c", "NullKey"));
            info.setTemp_k(keyValueMap.getOrDefault("temp_k", "NullKey"));
            info.setTemp_t0(keyValueMap.getOrDefault("temp_t0", "NullKey"));
            info.setSens_k(keyValueMap.getOrDefault("sens_k", "NullKey"));
            info.setTemp_b(keyValueMap.getOrDefault("temp_b", "NullKey"));
            info.setReferval_f(keyValueMap.getOrDefault("referval_f", "NullKey"));
            info.setElastic_mod(keyValueMap.getOrDefault("elastic_mod", "NullKey"));
            info.setLsycsds(keyValueMap.getOrDefault("lsycsds", "NullKey"));
            info.setLsyysst(keyValueMap.getOrDefault("lsyysst", "NullKey"));
            info.setInitvalx(keyValueMap.getOrDefault("initvalx", "NullKey"));
            info.setInitvaly(keyValueMap.getOrDefault("initvaly", "NullKey"));

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
