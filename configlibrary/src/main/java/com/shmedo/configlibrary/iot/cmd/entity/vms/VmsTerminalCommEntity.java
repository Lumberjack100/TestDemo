package com.shmedo.configlibrary.iot.cmd.entity.vms;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/2/20 <br/>
 * 描述：     Vms 终端通信参数
 */
public class VmsTerminalCommEntity implements Validater {
    private String sn;
    private String netid;//网络号
    private String dstaddr;//通道的地址
    private String channel;//通信信道
    private String airbaud;//空中波特率

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getNetid() {
        return netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getDstaddr() {
        return dstaddr;
    }

    public void setDstaddr(String dstaddr) {
        this.dstaddr = dstaddr;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAirbaud() {
        return airbaud;
    }

    public void setAirbaud(String airbaud) {
        this.airbaud = airbaud;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sn=" + sn);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(netid)) {
            stringBuilder.append("netid=" + netid);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(dstaddr)) {
            stringBuilder.append("dstaddr=" + dstaddr);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(channel)) {
            stringBuilder.append("channel=" + channel);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(airbaud)) {
            stringBuilder.append("airbaud=" + airbaud);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
