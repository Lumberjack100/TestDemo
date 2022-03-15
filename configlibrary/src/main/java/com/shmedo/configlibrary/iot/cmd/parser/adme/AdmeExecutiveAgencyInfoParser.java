package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/29/20 <br/>
 * 描述：  解析ADME执行机构参数
 */
public class AdmeExecutiveAgencyInfoParser implements IOTResultParser<AdmeExecutiveAgencyInfo> {
    @Override
    public AdmeExecutiveAgencyInfo parse(String result) {
        AdmeExecutiveAgencyInfo info = new AdmeExecutiveAgencyInfo();
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
            info.setMeastype(keyValueMap.getOrDefault("meastype", "NullKey"));
            info.setDatatype(keyValueMap.getOrDefault("datatype", "NullKey"));
            info.setDatareply(keyValueMap.getOrDefault("datareply", "NullKey"));
            info.setRoundwaitetime(keyValueMap.getOrDefault("roundwaitetime", "NullKey"));
            info.setRoundmeasinval(keyValueMap.getOrDefault("roundmeasinval", "NullKey"));
            info.setRoundmeasstart(keyValueMap.getOrDefault("roundmeasstart", "NullKey"));

            info.setDatainval(keyValueMap.getOrDefault("datainval", "NullKey"));
            info.setCompensatetime(keyValueMap.getOrDefault("compensatetime", "NullKey"));
            info.setDriveaddress(keyValueMap.getOrDefault("driveaddress", "NullKey"));
            info.setDownspeed(keyValueMap.getOrDefault("downspeed", "NullKey"));
            info.setInterdeep(keyValueMap.getOrDefault("interdeep", "NullKey"));
            info.setDownwaitetime(keyValueMap.getOrDefault("downwaitetime", "NullKey"));
            info.setUpspeed(keyValueMap.getOrDefault("upspeed", "NullKey"));
            info.setMeaspacing(keyValueMap.getOrDefault("measpacing", "NullKey"));
            info.setMeaintertime(keyValueMap.getOrDefault("meaintertime", "NullKey"));
            info.setMeabaseth(keyValueMap.getOrDefault("meabaseth", "NullKey"));
            info.setDwonblocked(keyValueMap.getOrDefault("dwonblocked", "NullKey"));
            info.setUntimenum(keyValueMap.getOrDefault("untimenum", "NullKey"));
            info.setDetectiontime(keyValueMap.getOrDefault("detectiontime", "NullKey"));
            info.setDetectionstart(keyValueMap.getOrDefault("detectionstart", "NullKey"));
            info.setDetectionend(keyValueMap.getOrDefault("detectionend", "NullKey"));
            info.setInterval_compensation(keyValueMap.getOrDefault("interval_compensation", "NullKey"));
            info.setInterval_fitting(keyValueMap.getOrDefault("interval_fitting", "NullKey"));
            info.setPoint_offset(keyValueMap.getOrDefault("point_offset", "NullKey"));

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
        return IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY;
    }
}
