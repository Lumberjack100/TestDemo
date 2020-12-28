package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/27/20 <br/>
 * 描述：    ADME 计米轮配置参数
 */
public class AdmeMeterWheelInfo {
    private String enclinenum;//编码器线数
    private String outline;//外径
    private String uptiona;//上拉一次修正参数
    private String uptionb;//上拉二次修正参数
    private String upconstant;//上拉常数
    private String upfilter;//上拉滤波器系数
    private String downtiona;//下放一次修正参数
    private String downtionb;//下放二次修正参数
    private String downconstant;//下放常数
    private String downfilter;//下放滤波器系数

    public String getEnclinenum() {
        return TextUtils.isEmpty(enclinenum) ? "" : enclinenum;
    }

    public void setEnclinenum(String enclinenum) {
        this.enclinenum = enclinenum;
    }

    public String getOutline() {
        return TextUtils.isEmpty(outline) ? "" : outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public String getUptiona() {
        return TextUtils.isEmpty(uptiona) ? "" : uptiona;
    }

    public void setUptiona(String uptiona) {
        this.uptiona = uptiona;
    }

    public String getUptionb() {
        return TextUtils.isEmpty(uptionb) ? "" : uptionb;
    }

    public void setUptionb(String uptionb) {
        this.uptionb = uptionb;
    }

    public String getUpconstant() {
        return TextUtils.isEmpty(upconstant) ? "" : upconstant;
    }

    public void setUpconstant(String upconstant) {
        this.upconstant = upconstant;
    }

    public String getUpfilter() {
        return TextUtils.isEmpty(upfilter) ? "" : upfilter;
    }

    public void setUpfilter(String upfilter) {
        this.upfilter = upfilter;
    }

    public String getDowntiona() {
        return TextUtils.isEmpty(downtiona) ? "" : downtiona;
    }

    public void setDowntiona(String downtiona) {
        this.downtiona = downtiona;
    }

    public String getDowntionb() {
        return TextUtils.isEmpty(downtionb) ? "" : downtionb;
    }

    public void setDowntionb(String downtionb) {
        this.downtionb = downtionb;
    }

    public String getDownconstant() {
        return TextUtils.isEmpty(downconstant) ? "" : downconstant;
    }

    public void setDownconstant(String downconstant) {
        this.downconstant = downconstant;
    }

    public String getDownfilter() {
        return TextUtils.isEmpty(downfilter) ? "" : downfilter;
    }

    public void setDownfilter(String downfilter) {
        this.downfilter = downfilter;
    }
}
