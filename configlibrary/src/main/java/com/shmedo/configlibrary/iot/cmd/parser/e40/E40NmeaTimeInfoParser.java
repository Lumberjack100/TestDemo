package com.shmedo.configlibrary.iot.cmd.parser.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.e40.E40NmeaTimeInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/4 <br/>
 * 描述：     TODO
 */
public class E40NmeaTimeInfoParser implements IOTResultParser<E40NmeaTimeInfo> {
    @Override
    public E40NmeaTimeInfo parse(String result) {
        E40NmeaTimeInfo info = new E40NmeaTimeInfo();
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
            info.setGga(TextUtils.isEmpty(keyValueMap.get("gga")) ? "" : keyValueMap.get("gga"));
            info.setRmc(TextUtils.isEmpty(keyValueMap.get("rmc")) ? "" : keyValueMap.get("rmc"));
            info.setVtg(TextUtils.isEmpty(keyValueMap.get("vtg")) ? "" : keyValueMap.get("vtg"));
            info.setGsv(TextUtils.isEmpty(keyValueMap.get("gsv")) ? "" : keyValueMap.get("gsv"));
            info.setGsa(TextUtils.isEmpty(keyValueMap.get("gsa")) ? "" : keyValueMap.get("gsa"));

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
        return IOTCommandType.E40_MD_GET_NMEA_TIME;

    }
}
