package com.shmedo.configlibrary.iot.model.vms;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/15/20 <br/>
 * 描述：   Vms网关通道的控制参数信息
 */
public class VmsAisleInfo {
    private int channel;//通道号

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
    private String terminalnum;//终端接入数量
    private String rssi;//信号强度


    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getNetid() {
        return TextUtils.isEmpty(netid) ? "" : netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getPpt() {
        return TextUtils.isEmpty(ppt) ? "" : ppt;
    }

    public void setPpt(String ppt) {
        this.ppt = ppt;
    }

    public String getAddr() {
        return TextUtils.isEmpty(addr) ? "" : addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getChl() {
        return TextUtils.isEmpty(chl) ? "" : chl;
    }

    public void setChl(String chl) {
        this.chl = chl;
    }

    public String getTerminalmode() {
        return TextUtils.isEmpty(terminalmode) ? "" : terminalmode;
    }

    public void setTerminalmode(String terminalmode) {
        this.terminalmode = terminalmode;
    }

    public String getSendgap() {
        return TextUtils.isEmpty(sendgap) ? "" : sendgap;
    }

    public void setSendgap(String sendgap) {
        this.sendgap = sendgap;
    }

    public String getOffline() {
        return TextUtils.isEmpty(offline) ? "" : offline;
    }

    public void setOffline(String offline) {
        this.offline = offline;
    }

    public String getSleepgap() {
        return TextUtils.isEmpty(sleepgap) ? "" : sleepgap;
    }

    public void setSleepgap(String sleepgap) {
        this.sleepgap = sleepgap;
    }

    public String getWakeupgap() {
        return TextUtils.isEmpty(wakeupgap) ? "" : wakeupgap;
    }

    public void setWakeupgap(String wakeupgap) {
        this.wakeupgap = wakeupgap;
    }

    public String getAirbaud() {
        return TextUtils.isEmpty(airbaud) ? "" : airbaud;
    }

    public void setAirbaud(String airbaud) {
        this.airbaud = airbaud;
    }

    public String getTerminalnum() {
        return TextUtils.isEmpty(terminalnum) ? "" : terminalnum;
    }

    public void setTerminalnum(String terminalnum) {
        this.terminalnum = terminalnum;
    }

    public String getRssi() {
        return TextUtils.isEmpty(rssi) ? "" : rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }
}
