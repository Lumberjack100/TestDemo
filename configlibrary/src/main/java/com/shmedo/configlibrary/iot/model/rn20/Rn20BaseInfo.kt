package com.shmedo.configlibrary.iot.model.rn20;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/3 <br/>
 * 描述：     TODO
 */
public class Rn20BaseInfo {
    private String sn;//SN号
    private String ver;//固件版本
    private String local;//位置
    private String involt;//内部电量
    private String ssi;//信号强度
    private String recvbuf;//接收数据包数
    private String sendbuf;//发送数据包数
    private String netid;//网络号
    private String addr;//终端地址
    private String channel;//通讯信道
    private String finaltime;//最后交互时间
    private String logintime;//终端注册时间

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getVer() {
        return TextUtils.isEmpty(ver) ? "" : ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getLocal() {
        return TextUtils.isEmpty(local) ? "" : local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getInvolt() {
        return TextUtils.isEmpty(involt) ? "" : involt;
    }

    public void setInvolt(String involt) {
        this.involt = involt;
    }

    public String getSsi() {
        return TextUtils.isEmpty(ssi) ? "" : ssi;
    }

    public void setSsi(String ssi) {
        this.ssi = ssi;
    }

    public String getRecvbuf() {
        return TextUtils.isEmpty(recvbuf) ? "" : recvbuf;
    }

    public void setRecvbuf(String recvbuf) {
        this.recvbuf = recvbuf;
    }

    public String getSendbuf() {
        return TextUtils.isEmpty(sendbuf) ? "" : sendbuf;
    }

    public void setSendbuf(String sendbuf) {
        this.sendbuf = sendbuf;
    }

    public String getNetid() {
        return TextUtils.isEmpty(netid) ? "" : netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getAddr() {
        return TextUtils.isEmpty(addr) ? "" : addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getChannel() {
        return TextUtils.isEmpty(channel) ? "" : channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getFinaltime() {
        return TextUtils.isEmpty(finaltime) ? "" : finaltime;
    }

    public void setFinaltime(String finaltime) {
        this.finaltime = finaltime;
    }

    public String getLogintime() {
        return TextUtils.isEmpty(logintime) ? "" : logintime;
    }

    public void setLogintime(String logintime) {
        this.logintime = logintime;
    }
}
