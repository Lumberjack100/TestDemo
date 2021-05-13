package com.shmedo.configlibrary.iot.cmd.parser.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasDataReportInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/20 <br/>
 * 描述：       解析DAS  数据上报时间参数
 */
public class DasDataReportInfoParser implements IOTResultParser<DasDataReportInfo> {
    @Override
    public DasDataReportInfo parse(String result) {
        DasDataReportInfo info = new DasDataReportInfo();
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
            info.setReport_intv(TextUtils.isEmpty(keyValueMap.get("report_intv")) ? "" : keyValueMap.get("report_intv"));
            info.setPlus_intv(TextUtils.isEmpty(keyValueMap.get("plus_intv")) ? "" : keyValueMap.get("plus_intv"));
            info.setPlus_count(TextUtils.isEmpty(keyValueMap.get("plus_count")) ? "" : keyValueMap.get("plus_count"));

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
        return IOTCommandType.DAS_MD_GET_DATA_REPORT_TIME;
    }
}
