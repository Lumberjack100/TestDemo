package com.shmedo.configlibrary.iot.cmd.parser.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.das.DasBaseInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：      解析DAS 状态页面基本信息参数
 */
public class DasBaseInfoParser implements IOTResultParser<DasBaseInfo> {
    @Override
    public DasBaseInfo parse(String result) {
        DasBaseInfo info = new DasBaseInfo();
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
            info.setSn(TextUtils.isEmpty(keyValueMap.get("sn")) ? "" : keyValueMap.get("sn"));
            info.setIccid(TextUtils.isEmpty(keyValueMap.get("iccid")) ? "" : keyValueMap.get("iccid"));
            info.setImei(TextUtils.isEmpty(keyValueMap.get("imei")) ? "" : keyValueMap.get("imei"));
            info.setVer(TextUtils.isEmpty(keyValueMap.get("ver")) ? "" : keyValueMap.get("ver"));
            info.setLocal(TextUtils.isEmpty(keyValueMap.get("local")) ? "" : keyValueMap.get("local"));
            info.setInvolt(TextUtils.isEmpty(keyValueMap.get("involt")) ? "" : keyValueMap.get("involt"));
            info.setOutvolt(TextUtils.isEmpty(keyValueMap.get("outvolt")) ? "" : keyValueMap.get("outvolt"));
            info.setCsq(TextUtils.isEmpty(keyValueMap.get("csq")) ? "" : keyValueMap.get("csq"));
            info.setIsp(TextUtils.isEmpty(keyValueMap.get("isp")) ? "" : keyValueMap.get("isp"));
            info.setCode(TextUtils.isEmpty(keyValueMap.get("code")) ? "" : keyValueMap.get("code"));

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
        return IOTCommandType.DAS_MD_GET_DEVICE_BASE;
    }
}
