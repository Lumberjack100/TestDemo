package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：     ADME 测斜仪配置参数
 */
public class AdmeInclinometerInfo {
    private String inctype;//测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    private String lowpower;//低功耗模式(0:关闭，1:开启)
    private String address;//采集器 / MAC 地址
    private String collinval;//采集器采集间隔
    private String calcinval;//采集器解算间隔
    private String dormancytime;//休眠时间
    private String interupdate;//测斜仪修正值

    public String getInctype() {
        return TextUtils.isEmpty(inctype) ? "" : inctype;
    }

    public void setInctype(String inctype) {
        this.inctype = inctype;
    }

    public String getLowpower() {
        return TextUtils.isEmpty(lowpower) ? "" : lowpower;
    }

    public void setLowpower(String lowpower) {
        this.lowpower = lowpower;
    }

    public String getAddress() {
        return TextUtils.isEmpty(address) ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCollinval() {
        return TextUtils.isEmpty(collinval) ? "" : collinval;
    }

    public void setCollinval(String collinval) {
        this.collinval = collinval;
    }

    public String getCalcinval() {
        return TextUtils.isEmpty(calcinval) ? "" : calcinval;
    }

    public void setCalcinval(String calcinval) {
        this.calcinval = calcinval;
    }

    public String getDormancytime() {
        return TextUtils.isEmpty(dormancytime) ? "" : dormancytime;
    }

    public void setDormancytime(String dormancytime) {
        this.dormancytime = dormancytime;
    }

    public String getInterupdate() {
        return TextUtils.isEmpty(interupdate) ? "" : interupdate;
    }

    public void setInterupdate(String interupdate) {
        this.interupdate = interupdate;
    }
}
