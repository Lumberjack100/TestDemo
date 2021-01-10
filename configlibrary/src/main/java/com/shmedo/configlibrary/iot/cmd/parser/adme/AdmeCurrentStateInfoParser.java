package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeCurrentStateInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：      解析 ADME 设备当前状态
 */
public class AdmeCurrentStateInfoParser implements IOTResultParser<AdmeCurrentStateInfo> {
    @Override
    public AdmeCurrentStateInfo parse(String result) {
        AdmeCurrentStateInfo info = new AdmeCurrentStateInfo();
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
            info.setProductid(TextUtils.isEmpty(keyValueMap.get("productid")) ? "" : keyValueMap.get("productid"));
            info.setSimid(TextUtils.isEmpty(keyValueMap.get("simid")) ? "" : keyValueMap.get("simid"));
            info.setImeid(TextUtils.isEmpty(keyValueMap.get("imeid")) ? "" : keyValueMap.get("imeid"));
            info.setFirversion(TextUtils.isEmpty(keyValueMap.get("firversion")) ? "" : keyValueMap.get("firversion"));
            info.setCtrinputv(TextUtils.isEmpty(keyValueMap.get("ctrinputv")) ? "" : keyValueMap.get("ctrinputv"));
            info.setDriveinputv(TextUtils.isEmpty(keyValueMap.get("driveinputv")) ? "" : keyValueMap.get("driveinputv"));
            info.setInctype(TextUtils.isEmpty(keyValueMap.get("inctype")) ? "" : keyValueMap.get("inctype"));
            info.setIncnum(TextUtils.isEmpty(keyValueMap.get("incnum")) ? "" : keyValueMap.get("incnum"));
            info.setIncvoltage(TextUtils.isEmpty(keyValueMap.get("incvoltage")) ? "" : keyValueMap.get("incvoltage"));
            info.setTemperature(TextUtils.isEmpty(keyValueMap.get("temperature")) ? "" : keyValueMap.get("temperature"));
            info.setHumidity(TextUtils.isEmpty(keyValueMap.get("humidity")) ? "" : keyValueMap.get("humidity"));
            info.setIntertempe(TextUtils.isEmpty(keyValueMap.get("intertempe")) ? "" : keyValueMap.get("intertempe"));
            info.setSignalstr(TextUtils.isEmpty(keyValueMap.get("signalstr")) ? "" : keyValueMap.get("signalstr"));
            info.setIncloc(TextUtils.isEmpty(keyValueMap.get("incloc")) ? "" : keyValueMap.get("incloc"));
            info.setAbndiasis(TextUtils.isEmpty(keyValueMap.get("abndiasis")) ? "" : keyValueMap.get("abndiasis"));
            info.setDownnum(TextUtils.isEmpty(keyValueMap.get("downnum")) ? "" : keyValueMap.get("downnum"));
            info.setTestway(TextUtils.isEmpty(keyValueMap.get("testway")) ? "" : keyValueMap.get("testway"));

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
        return IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE;
    }
}
