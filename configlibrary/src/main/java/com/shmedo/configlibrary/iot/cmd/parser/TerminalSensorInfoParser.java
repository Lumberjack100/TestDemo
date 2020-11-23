package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.TerminalSensorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/23/20 <br/>
 * 描述：     TODO
 */
public class TerminalSensorInfoParser implements IOTResultParser<TerminalSensorInfo> {
    @Override
    public TerminalSensorInfo parse(String result) {
        TerminalSensorInfo info = new TerminalSensorInfo();
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
            info.setChannel(TextUtils.isEmpty(keyValueMap.get("channel")) ? "" : keyValueMap.get("channel"));
            info.setInstert(TextUtils.isEmpty(keyValueMap.get("instert")) ? "" : keyValueMap.get("instert"));
            info.setFreqtype(TextUtils.isEmpty(keyValueMap.get("freqtype")) ? "" : keyValueMap.get("freqtype"));
            info.setFreqmax(TextUtils.isEmpty(keyValueMap.get("freqmax")) ? "" : keyValueMap.get("freqmax"));
            info.setFreqmin(TextUtils.isEmpty(keyValueMap.get("freqmin")) ? "" : keyValueMap.get("freqmin"));
            info.setVolttype(TextUtils.isEmpty(keyValueMap.get("volttype")) ? "" : keyValueMap.get("volttype"));
            info.setExpvolt(TextUtils.isEmpty(keyValueMap.get("expvolt")) ? "" : keyValueMap.get("expvolt"));
            info.setType(TextUtils.isEmpty(keyValueMap.get("type")) ? "" : keyValueMap.get("type"));
            info.setName(TextUtils.isEmpty(keyValueMap.get("name")) ? "" : keyValueMap.get("name"));
            info.setGateval(TextUtils.isEmpty(keyValueMap.get("gateval")) ? "" : keyValueMap.get("gateval"));
            info.setCorral(TextUtils.isEmpty(keyValueMap.get("corral")) ? "" : keyValueMap.get("corral"));
            info.setFixsite(TextUtils.isEmpty(keyValueMap.get("fixsite")) ? "" : keyValueMap.get("fixsite"));
            info.setRopelen(TextUtils.isEmpty(keyValueMap.get("ropelen")) ? "" : keyValueMap.get("ropelen"));
            info.setParama(TextUtils.isEmpty(keyValueMap.get("parama")) ? "" : keyValueMap.get("parama"));
            info.setParamb(TextUtils.isEmpty(keyValueMap.get("paramb")) ? "" : keyValueMap.get("paramb"));
            info.setParamc(TextUtils.isEmpty(keyValueMap.get("paramc")) ? "" : keyValueMap.get("paramc"));
            info.setParamk(TextUtils.isEmpty(keyValueMap.get("paramk")) ? "" : keyValueMap.get("paramk"));
            info.setParamm(TextUtils.isEmpty(keyValueMap.get("paramm")) ? "" : keyValueMap.get("paramm"));
            info.setParamf(TextUtils.isEmpty(keyValueMap.get("paramf")) ? "" : keyValueMap.get("paramf"));
            info.setParamt(TextUtils.isEmpty(keyValueMap.get("paramt")) ? "" : keyValueMap.get("paramt"));

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
        return IOTCommandType.MD_GET_TERMINAL_CHL;
    }
}
