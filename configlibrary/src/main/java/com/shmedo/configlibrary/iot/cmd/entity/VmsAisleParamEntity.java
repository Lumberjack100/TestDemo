package com.shmedo.configlibrary.iot.cmd.entity;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/15/20 <br/>
 * 描述：     TODO
 */
public class VmsAisleParamEntity implements Validater {
    private VmsAisleNumber vmsAisleNumber;

    private String netid;//网关配置通道的网络号,取值[1,65535]，默认为1
    private String ppt;//空中唤醒时间，取值[0,5]s，默认2，当为0时，lora模块不休眠
    private String addr;//网关配置通道的地址,取值[0,63]，channel=0，默认为1；channel=1，默认为2；channel=2，默认为3
    private String chl;//通信信道，取值[0,31]，channel=0，默认为23；channel=1，默认为20；channel=2，默认为26
    private String terminalmode;//终端工作模式，取值[0,1],0低功耗模式，1正常模式
    private String sendgap;//指令发送间隔，取值≥3s，默认为3
    private String offline;//存活时间（终端长时间无数据，网关将终端删除），取值≥7200s，默认43200s
    private String sleepgap;//终端休眠时间，取值[0,5]s，默认为2
    private String wakeupgap;//终端唤醒时间，取值[0,65535]ms，默认100
    private String airbaud;//空中速率，取值[1~6]

    public VmsAisleNumber getVmsAisleNumber() {
        return vmsAisleNumber;
    }

    public void setVmsAisleNumber(VmsAisleNumber vmsAisleNumber) {
        this.vmsAisleNumber = vmsAisleNumber;
    }

    public String getNetid() {
        return netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getPpt() {
        return ppt;
    }

    public void setPpt(String ppt) {
        this.ppt = ppt;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getChl() {
        return chl;
    }

    public void setChl(String chl) {
        this.chl = chl;
    }

    public String getTerminalmode() {
        return terminalmode;
    }

    public void setTerminalmode(String terminalmode) {
        this.terminalmode = terminalmode;
    }

    public String getSendgap() {
        return sendgap;
    }

    public void setSendgap(String sendgap) {
        this.sendgap = sendgap;
    }

    public String getOffline() {
        return offline;
    }

    public void setOffline(String offline) {
        this.offline = offline;
    }

    public String getSleepgap() {
        return sleepgap;
    }

    public void setSleepgap(String sleepgap) {
        this.sleepgap = sleepgap;
    }

    public String getWakeupgap() {
        return wakeupgap;
    }

    public void setWakeupgap(String wakeupgap) {
        this.wakeupgap = wakeupgap;
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
        stringBuilder.append("channel=" + vmsAisleNumber.toInt());
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(netid)) {
            stringBuilder.append("netid=" + netid);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(ppt)) {
            stringBuilder.append("ppt=" + ppt);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(addr)) {
            stringBuilder.append("addr=" + addr);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(chl)) {
            stringBuilder.append("chl=" + chl);
            stringBuilder.append("&");
        }

        stringBuilder.append("terminalmode=" + terminalmode);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(sendgap)) {
            stringBuilder.append("sendgap=" + sendgap);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(offline)) {
            stringBuilder.append("offline=" + offline);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(sleepgap)) {
            stringBuilder.append("sleepgap=" + sleepgap);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(wakeupgap)) {
            stringBuilder.append("wakeupgap=" + wakeupgap);
            stringBuilder.append("&");
        }

        stringBuilder.append("airbaud=" + airbaud);

        return stringBuilder.toString();
    }
}
