package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/19 <br/>
 * 描述：    数字式渗压计参数
 */
public class DasDigitalPiezometerInfo {
    private String sw;//0：关闭数字式渗压计采集功能 1：打开数字式渗压计采集功能
    private String threshold;//触发阈值
    private String corrval;//修正值
    private String ropelen;//绳长（渗压计到管口的距离）
    private String tubealti;//管口高程

    public String getSw() {
        return TextUtils.isEmpty(sw) ? "" : sw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }

    public String getThreshold() {
        return TextUtils.isEmpty(threshold) ? "" : threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getCorrval() {
        return TextUtils.isEmpty(corrval) ? "" : corrval;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }

    public String getRopelen() {
        return TextUtils.isEmpty(ropelen) ? "" : ropelen;
    }

    public void setRopelen(String ropelen) {
        this.ropelen = ropelen;
    }

    public String getTubealti() {
        return TextUtils.isEmpty(tubealti) ? "" : tubealti;
    }

    public void setTubealti(String tubealti) {
        this.tubealti = tubealti;
    }
}
