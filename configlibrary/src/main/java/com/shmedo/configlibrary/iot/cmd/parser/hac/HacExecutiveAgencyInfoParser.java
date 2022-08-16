package com.shmedo.configlibrary.iot.cmd.parser.hac;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.hac.HacExecutiveAgencyInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/29 <br/>
 * 描述：     TODO
 */
public class HacExecutiveAgencyInfoParser implements IOTResultParser<HacExecutiveAgencyInfo> {
    @Override
    public HacExecutiveAgencyInfo parse(String result) {
        HacExecutiveAgencyInfo info = new HacExecutiveAgencyInfo();
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
            info.setDatatype(keyValueMap.getOrDefault("datatype", "NullKey"));
            info.setDatareply(keyValueMap.getOrDefault("datareply", "NullKey"));
            info.setDatainval(keyValueMap.getOrDefault("datainval", "NullKey"));
            info.setCompensatetime(keyValueMap.getOrDefault("compensatetime", "NullKey"));
            info.setDriveaddress(keyValueMap.getOrDefault("driveaddress", "NullKey"));
            info.setDownspeed(keyValueMap.getOrDefault("downspeed", "NullKey"));
            info.setDownwaitetime(keyValueMap.getOrDefault("downwaitetime", "NullKey"));
            info.setUpspeed(keyValueMap.getOrDefault("upspeed", "NullKey"));
            info.setMeaspacing(keyValueMap.getOrDefault("measpacing", "NullKey"));
            info.setMeaintertime(keyValueMap.getOrDefault("meaintertime", "NullKey"));
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
        return IOTCommandType.ADME_HAC_MD_GET_EXECUTIVE_AGENCY;
    }
}
