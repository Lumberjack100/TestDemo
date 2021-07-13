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

            info.setDatatype(!keyValueMap.containsKey("datatype") ? "NullKey" : keyValueMap.get("datatype"));
            info.setDatareply(!keyValueMap.containsKey("datareply") ? "NullKey" : keyValueMap.get("datareply"));
            info.setRoundwaitetime(!keyValueMap.containsKey("roundwaitetime") ? "NullKey" : keyValueMap.get("roundwaitetime"));
            info.setDatainval(!keyValueMap.containsKey("datainval") ? "NullKey" : keyValueMap.get("datainval"));
            info.setCompensatetime(!keyValueMap.containsKey("compensatetime") ? "NullKey": keyValueMap.get("compensatetime"));
            info.setDriveaddress(!keyValueMap.containsKey("driveaddress") ? "NullKey" : keyValueMap.get("driveaddress"));
            info.setDownspeed(!keyValueMap.containsKey("downspeed") ? "NullKey" : keyValueMap.get("downspeed"));
            info.setInterdeep(!keyValueMap.containsKey("interdeep") ? "NullKey" : keyValueMap.get("interdeep"));
            info.setDownwaitetime(!keyValueMap.containsKey("downwaitetime") ? "NullKey" : keyValueMap.get("downwaitetime"));
            info.setUpspeed(!keyValueMap.containsKey("upspeed") ? "NullKey" : keyValueMap.get("upspeed"));
            info.setMeaspacing(!keyValueMap.containsKey("measpacing") ? "NullKey" : keyValueMap.get("measpacing"));
            info.setMeaintertime(!keyValueMap.containsKey("meaintertime") ? "NullKey" : keyValueMap.get("meaintertime"));
            info.setMeabaseth(!keyValueMap.containsKey("meabaseth") ? "NullKey" : keyValueMap.get("meabaseth"));
            info.setDwonblocked(!keyValueMap.containsKey("dwonblocked") ? "NullKey" : keyValueMap.get("dwonblocked"));
            info.setUntimenum(!keyValueMap.containsKey("untimenum") ? "NullKey" : keyValueMap.get("untimenum"));
            info.setDetectiontime(!keyValueMap.containsKey("detectiontime") ? "NullKey" : keyValueMap.get("detectiontime"));
            info.setDetectionstart(!keyValueMap.containsKey("detectionstart") ? "NullKey" : keyValueMap.get("detectionstart"));
            info.setDetectionend(!keyValueMap.containsKey("detectionend") ? "NullKey" : keyValueMap.get("detectionend"));
            info.setInterval_compensation(!keyValueMap.containsKey("interval_compensation") ? "NullKey": keyValueMap.get("interval_compensation"));
            info.setInterval_fitting(!keyValueMap.containsKey("interval_fitting") ? "NullKey" : keyValueMap.get("interval_fitting"));
            info.setPoint_offset(!keyValueMap.containsKey("point_offset") ? "NullKey" : keyValueMap.get("point_offset"));

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
