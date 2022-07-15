package com.shmedo.configlibrary.iot.cmd.parser.e40;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.e40.E40EthernetInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：     解析 有线网络参数
 */
public class E40EthernetInfoParser implements IOTResultParser<E40EthernetInfo> {
    @Override
    public E40EthernetInfo parse(String result) {
        E40EthernetInfo info = new E40EthernetInfo();
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
            info.setDhcp(keyValueMap.getOrDefault("dhcp", "NullKey"));
            info.setIp(keyValueMap.getOrDefault("ip", "NullKey"));
            info.setNetmask(keyValueMap.getOrDefault("netmask", "NullKey"));
            info.setGateway(keyValueMap.getOrDefault("gateway", "NullKey"));
            info.setDns(keyValueMap.getOrDefault("dns", "NullKey"));

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
        return IOTCommandType.E40_MD_GET_ETHERNET;
    }
}
