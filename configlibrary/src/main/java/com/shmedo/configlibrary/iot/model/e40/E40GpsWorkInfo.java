package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/6/18 <br/>
 * 描述：     E40 GPS 工作参数
 */
public class E40GpsWorkInfo {
    private String cutoffangle;//卫星仰角截止角 范围0-90度
    private String range;//观测范围  0或1
    private String savefreq;//数据频率

    public String getCutoffangle() {
        return TextUtils.isEmpty(cutoffangle) ? "" : cutoffangle;
    }

    public void setCutoffangle(String cutoffangle) {
        this.cutoffangle = cutoffangle;
    }

    public String getRange() {
        return TextUtils.isEmpty(range) ? "" : range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getSavefreq() {
        return TextUtils.isEmpty(savefreq) ? "" : savefreq;
    }

    public void setSavefreq(String savefreq) {
        this.savefreq = savefreq;
    }
}
