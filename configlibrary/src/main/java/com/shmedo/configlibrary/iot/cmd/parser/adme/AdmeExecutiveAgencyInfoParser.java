package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

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
            info.setDatatype(TextUtils.isEmpty(keyValueMap.get("datatype")) ? "" : keyValueMap.get("datatype"));
            info.setDatareply(TextUtils.isEmpty(keyValueMap.get("datareply")) ? "" : keyValueMap.get("datareply"));
            info.setRoundwaitetime(TextUtils.isEmpty(keyValueMap.get("roundwaitetime")) ? "" : keyValueMap.get("roundwaitetime"));
            info.setDatainval(TextUtils.isEmpty(keyValueMap.get("datainval")) ? "" : keyValueMap.get("datainval"));
            info.setCompensatetime(TextUtils.isEmpty(keyValueMap.get("compensatetime")) ? "" : keyValueMap.get("compensatetime"));
            info.setDriveaddress(TextUtils.isEmpty(keyValueMap.get("driveaddress")) ? "" : keyValueMap.get("driveaddress"));
            info.setDownspeed(TextUtils.isEmpty(keyValueMap.get("downspeed")) ? "" : keyValueMap.get("downspeed"));
            info.setInterdeep(TextUtils.isEmpty(keyValueMap.get("interdeep")) ? "" : keyValueMap.get("interdeep"));
            info.setDownwaitetime(TextUtils.isEmpty(keyValueMap.get("downwaitetime")) ? "" : keyValueMap.get("downwaitetime"));
            info.setUpspeed(TextUtils.isEmpty(keyValueMap.get("upspeed")) ? "" : keyValueMap.get("upspeed"));
            info.setMeaspacing(TextUtils.isEmpty(keyValueMap.get("measpacing")) ? "" : keyValueMap.get("measpacing"));
            info.setMeaintertime(TextUtils.isEmpty(keyValueMap.get("meaintertime")) ? "" : keyValueMap.get("meaintertime"));
            info.setMeabaseth(TextUtils.isEmpty(keyValueMap.get("meabaseth")) ? "" : keyValueMap.get("meabaseth"));
            info.setDwonblocked(TextUtils.isEmpty(keyValueMap.get("dwonblocked")) ? "" : keyValueMap.get("dwonblocked"));
            info.setUntimenum(TextUtils.isEmpty(keyValueMap.get("untimenum")) ? "" : keyValueMap.get("untimenum"));
            info.setDetectiontime(TextUtils.isEmpty(keyValueMap.get("detectiontime")) ? "" : keyValueMap.get("detectiontime"));
            info.setDetectionstart(TextUtils.isEmpty(keyValueMap.get("detectionstart")) ? "" : keyValueMap.get("detectionstart"));
            info.setDetectionend(TextUtils.isEmpty(keyValueMap.get("detectionend")) ? "" : keyValueMap.get("detectionend"));

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
