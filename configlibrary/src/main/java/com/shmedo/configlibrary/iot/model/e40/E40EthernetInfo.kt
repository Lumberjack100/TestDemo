package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：    E40 有线网络参数信息
 */
public class E40EthernetInfo {
    private String dhcp;
    private String ip;
    private String netmask;//子网掩码
    private String gateway;
    private String dns;

    public String getDhcp() {
        return TextUtils.isEmpty(dhcp) ? "" : dhcp;
    }

    public void setDhcp(String dhcp) {
        this.dhcp = dhcp;
    }

    public String getIp() {
        return TextUtils.isEmpty(ip) ? "" : ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNetmask() {
        return TextUtils.isEmpty(netmask) ? "" : netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public String getGateway() {
        return TextUtils.isEmpty(gateway) ? "" : gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getDns() {
        return TextUtils.isEmpty(dns) ? "" : dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }
}
