package com.shmedo.configlibrary.iot.cmd.entity.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/27/20 <br/>
 * 描述：    生成ADME 计米轮配置参数拼接指令
 */
public class AdmeMeterWheelEntity implements Validater {
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

    public void setEnclinenum(String enclinenum) {
        this.enclinenum = enclinenum;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public void setUptiona(String uptiona) {
        this.uptiona = uptiona;
    }

    public void setUptionb(String uptionb) {
        this.uptionb = uptionb;
    }

    public void setUpconstant(String upconstant) {
        this.upconstant = upconstant;
    }

    public void setUpfilter(String upfilter) {
        this.upfilter = upfilter;
    }

    public void setDowntiona(String downtiona) {
        this.downtiona = downtiona;
    }

    public void setDowntionb(String downtionb) {
        this.downtionb = downtionb;
    }

    public void setDownconstant(String downconstant) {
        this.downconstant = downconstant;
    }

    public void setDownfilter(String downfilter) {
        this.downfilter = downfilter;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        if (!TextUtils.isEmpty(enclinenum)) {
            stringBuilder.append("enclinenum=" + enclinenum);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(outline)) {
            stringBuilder.append("outline=" + outline);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(uptiona)) {
            stringBuilder.append("uptiona=" + uptiona);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(uptionb)) {
            stringBuilder.append("uptionb=" + uptionb);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(upconstant)) {
            stringBuilder.append("upconstant=" + upconstant);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(upfilter)) {
            stringBuilder.append("upfilter=" + upfilter);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downtiona)) {
            stringBuilder.append("downtiona=" + downtiona);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downtionb)) {
            stringBuilder.append("downtionb=" + downtionb);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downconstant)) {
            stringBuilder.append("downconstant=" + downconstant);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downfilter)) {
            stringBuilder.append("downfilter=" + downfilter);
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
