package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/24/20 <br/>
 * 描述：    ADME 基础配置参数
 */
public class AdmeBasicConfigInfo {
    private String inctype;//测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    private String address;//采集器 / MAC 地址
    private String interdeep;//测斜管孔深
    private String downspeed;//下放速度
    private String downwaitetime;//下放等待时间
    private String datatype;//数据结算方式（0:顶固定法，1底固定法）

    public String getInctype() {
        return TextUtils.isEmpty(inctype) ? "" : inctype;
    }

    public void setInctype(String inctype) {
        this.inctype = inctype;
    }

    public String getAddress() {
        return TextUtils.isEmpty(address) ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInterdeep() {
        return TextUtils.isEmpty(interdeep) ? "" : interdeep;
    }

    public void setInterdeep(String interdeep) {
        this.interdeep = interdeep;
    }

    public String getDownspeed() {
        return TextUtils.isEmpty(downspeed) ? "" : downspeed;
    }

    public void setDownspeed(String downspeed) {
        this.downspeed = downspeed;
    }

    public String getDownwaitetime() {
        return TextUtils.isEmpty(downwaitetime) ? "" : downwaitetime;
    }

    public void setDownwaitetime(String downwaitetime) {
        this.downwaitetime = downwaitetime;
    }

    public String getDatatype() {
        return TextUtils.isEmpty(datatype) ? "" : datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
