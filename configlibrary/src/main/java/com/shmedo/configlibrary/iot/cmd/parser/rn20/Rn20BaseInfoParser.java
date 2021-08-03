package com.shmedo.configlibrary.iot.cmd.parser.rn20;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.rn20.Rn20BaseInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/3 <br/>
 * 描述：     解析雨量采集器基本信息
 */
public class Rn20BaseInfoParser implements IOTResultParser<Rn20BaseInfo> {
    @Override
    public Rn20BaseInfo parse(String result) {
        Rn20BaseInfo info = new Rn20BaseInfo();
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
            info.setVer(TextUtils.isEmpty(keyValueMap.get("ver")) ? "" : keyValueMap.get("ver"));
            info.setLocal(TextUtils.isEmpty(keyValueMap.get("local")) ? "" : keyValueMap.get("local"));
            info.setInvolt(TextUtils.isEmpty(keyValueMap.get("involt")) ? "" : keyValueMap.get("involt"));
            info.setSsi(TextUtils.isEmpty(keyValueMap.get("ssi")) ? "" : keyValueMap.get("ssi"));
            info.setRecvbuf(TextUtils.isEmpty(keyValueMap.get("recvbuf")) ? "" : keyValueMap.get("recvbuf"));
            info.setSendbuf(TextUtils.isEmpty(keyValueMap.get("sendbuf")) ? "" : keyValueMap.get("sendbuf"));
            info.setNetid(TextUtils.isEmpty(keyValueMap.get("netid")) ? "" : keyValueMap.get("netid"));
            info.setAddr(TextUtils.isEmpty(keyValueMap.get("addr")) ? "" : keyValueMap.get("addr"));
            info.setChannel(TextUtils.isEmpty(keyValueMap.get("channel")) ? "" : keyValueMap.get("channel"));
            info.setFinaltime(TextUtils.isEmpty(keyValueMap.get("finaltime")) ? "" : keyValueMap.get("finaltime"));
            info.setLogintime(TextUtils.isEmpty(keyValueMap.get("logintime")) ? "" : keyValueMap.get("logintime"));

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
        return IOTCommandType.RN20_MD_GET_TERMINAL_BASE;
    }
}
