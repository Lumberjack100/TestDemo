package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeterWheelInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/27/20 <br/>
 * 描述：      解析ADME计米轮配置参数
 */
public class AdmeMeterWheelParamParser implements IOTResultParser<AdmeMeterWheelInfo> {
    @Override
    public AdmeMeterWheelInfo parse(String result) {
        AdmeMeterWheelInfo info = new AdmeMeterWheelInfo();
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
            info.setEnclinenum(!keyValueMap.containsKey("enclinenum") ? "NullKey" : keyValueMap.get("enclinenum"));
            info.setOutline(!keyValueMap.containsKey("outline") ? "NullKey" : keyValueMap.get("outline"));
            info.setUptiona(!keyValueMap.containsKey("uptiona") ? "NullKey" : keyValueMap.get("uptiona"));
            info.setUptionb(!keyValueMap.containsKey("uptionb") ? "NullKey" : keyValueMap.get("uptionb"));
            info.setUpconstant(!keyValueMap.containsKey("upconstant") ? "NullKey" : keyValueMap.get("upconstant"));
            info.setUpfilter(!keyValueMap.containsKey("upfilter") ? "NullKey" : keyValueMap.get("upfilter"));
            info.setDowntiona(!keyValueMap.containsKey("downtiona") ? "NullKey" : keyValueMap.get("downtiona"));
            info.setDowntionb(!keyValueMap.containsKey("downtionb") ? "NullKey" : keyValueMap.get("downtionb"));
            info.setDownconstant(!keyValueMap.containsKey("downconstant") ? "NullKey" : keyValueMap.get("downconstant"));
            info.setDownfilter(!keyValueMap.containsKey("downfilter") ? "NullKey" : keyValueMap.get("downfilter"));

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
        return IOTCommandType.ADME_MD_GET_METER_WHEEL;
    }
}
