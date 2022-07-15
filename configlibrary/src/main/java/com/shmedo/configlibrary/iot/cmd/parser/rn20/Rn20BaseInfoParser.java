package com.shmedo.configlibrary.iot.cmd.parser.rn20;

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
            info.setSn(keyValueMap.getOrDefault("sn", "NullKey"));
            info.setVer(keyValueMap.getOrDefault("ver", "NullKey"));
            info.setLocal(keyValueMap.getOrDefault("local", "NullKey"));
            info.setInvolt(keyValueMap.getOrDefault("involt", "NullKey"));
            info.setSsi(keyValueMap.getOrDefault("ssi", "NullKey"));
            info.setRecvbuf(keyValueMap.getOrDefault("recvbuf", "NullKey"));
            info.setSendbuf(keyValueMap.getOrDefault("sendbuf", "NullKey"));
            info.setNetid(keyValueMap.getOrDefault("netid", "NullKey"));
            info.setAddr(keyValueMap.getOrDefault("addr", "NullKey"));
            info.setChannel(keyValueMap.getOrDefault("channel", "NullKey"));
            info.setFinaltime(keyValueMap.getOrDefault("finaltime", "NullKey"));
            info.setLogintime(keyValueMap.getOrDefault("logintime", "NullKey"));


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
