package com.shmedo.configlibrary.iot.cmd.entity.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/19 <br/>
 * 描述：     生成DAS 数字式渗压计参数拼接指令
 */
public class DasDigitalPiezometerEntity implements Validater {
    private String sw;//0：关闭数字式渗压计采集功能 1：打开数字式渗压计采集功能
    private String threshold;//触发阈值
    private String corrval;//修正值
    private String ropelen;//绳长（渗压计到管口的距离）
    private String tubealti;//管口高程

    public void setSw(String sw) {
        this.sw = sw;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }

    public void setRopelen(String ropelen) {
        this.ropelen = ropelen;
    }

    public void setTubealti(String tubealti) {
        this.tubealti = tubealti;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sw=" + sw);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(threshold)) {
            stringBuilder.append("threshold=" + threshold);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(corrval)) {
            stringBuilder.append("corrval=" + corrval);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(ropelen)) {
            stringBuilder.append("ropelen=" + ropelen);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(tubealti)) {
            stringBuilder.append("tubealti=" + tubealti);
            stringBuilder.append("&");
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
