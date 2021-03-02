package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/2/21 <br/>
 * 描述：     ADME的电机运动堵转缓停参数
 */
public class AdmeLockedRotorDetectionInfo {
    private String lowtbtss;//下放堵转缓停（0:关闭，1:开启）
    private String numpput;//单位时间脉冲数
    private String pdajtime;//脉冲检测判断时间
    private String detintiona;//堵转检测区间起始值
    private String detintionb;//堵转检测区间终值
    private String lowtorblothr;//下放力矩堵转阈值
    private String lowtordetime;//下放力矩检测判断时间
    private String lowsusrana;//下放缓停区间起始值
    private String lowsusranb;//下放缓停区间终值
    private String uptbtss;//上拉堵转缓停（0:关闭，1:开启）
    private String uptorblothr;//上拉力矩堵转阈值
    private String uptordetime;//上拉力矩检测判断时间
    private String upsusrana;//上拉缓停区间起始值
    private String upsusranb;//上拉缓停区间终值

    public String getLowtbtss() {
        return TextUtils.isEmpty(lowtbtss) ? "" : lowtbtss;
    }

    public void setLowtbtss(String lowtbtss) {
        this.lowtbtss = lowtbtss;
    }

    public String getNumpput() {
        return TextUtils.isEmpty(numpput) ? "" : numpput;
    }

    public void setNumpput(String numpput) {
        this.numpput = numpput;
    }

    public String getPdajtime() {
        return TextUtils.isEmpty(pdajtime) ? "" : pdajtime;
    }

    public void setPdajtime(String pdajtime) {
        this.pdajtime = pdajtime;
    }

    public String getDetintiona() {
        return TextUtils.isEmpty(detintiona) ? "" : detintiona;
    }

    public void setDetintiona(String detintiona) {
        this.detintiona = detintiona;
    }

    public String getDetintionb() {
        return TextUtils.isEmpty(detintionb) ? "" : detintionb;
    }

    public void setDetintionb(String detintionb) {
        this.detintionb = detintionb;
    }

    public String getLowtorblothr() {
        return TextUtils.isEmpty(lowtorblothr) ? "" : lowtorblothr;
    }

    public void setLowtorblothr(String lowtorblothr) {
        this.lowtorblothr = lowtorblothr;
    }

    public String getLowtordetime() {
        return TextUtils.isEmpty(lowtordetime) ? "" : lowtordetime;
    }

    public void setLowtordetime(String lowtordetime) {
        this.lowtordetime = lowtordetime;
    }

    public String getLowsusrana() {
        return TextUtils.isEmpty(lowsusrana) ? "" : lowsusrana;
    }

    public void setLowsusrana(String lowsusrana) {
        this.lowsusrana = lowsusrana;
    }

    public String getLowsusranb() {
        return TextUtils.isEmpty(lowsusranb) ? "" : lowsusranb;
    }

    public void setLowsusranb(String lowsusranb) {
        this.lowsusranb = lowsusranb;
    }

    public String getUptbtss() {
        return TextUtils.isEmpty(uptbtss) ? "" : uptbtss;
    }

    public void setUptbtss(String uptbtss) {
        this.uptbtss = uptbtss;
    }

    public String getUptorblothr() {
        return TextUtils.isEmpty(uptorblothr) ? "" : uptorblothr;
    }

    public void setUptorblothr(String uptorblothr) {
        this.uptorblothr = uptorblothr;
    }

    public String getUptordetime() {
        return TextUtils.isEmpty(uptordetime) ? "" : uptordetime;
    }

    public void setUptordetime(String uptordetime) {
        this.uptordetime = uptordetime;
    }

    public String getUpsusrana() {
        return TextUtils.isEmpty(upsusrana) ? "" : upsusrana;
    }

    public void setUpsusrana(String upsusrana) {
        this.upsusrana = upsusrana;
    }

    public String getUpsusranb() {
        return TextUtils.isEmpty(upsusranb) ? "" : upsusranb;
    }

    public void setUpsusranb(String upsusranb) {
        this.upsusranb = upsusranb;
    }
}
