package com.shmedo.configlibrary.iot.cmd.entity.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：     TODO
 */
public class E40EthernetEntity implements Validater {
    private String dhcp;
    private String ip;
    private String netmask;//子网掩码
    private String gateway;
    private String dns;

    public void setDhcp(String dhcp) {
        this.dhcp = dhcp;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("dhcp=" + dhcp);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(ip)) {
            stringBuilder.append("ip=" + ip);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(netmask)) {
            stringBuilder.append("netmask=" + netmask);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(gateway)) {
            stringBuilder.append("gateway=" + gateway);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(dns)) {
            stringBuilder.append("dns=" + dns);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
