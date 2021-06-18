package com.shmedo.configlibrary.iot.cmd.entity.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/6/18 <br/>
 * 描述：       生成GPS 工作参数拼接指令
 */
public class E40GpsWorkInfoEntity implements Validater {
    private String cutoffangle;//卫星仰角截止角 范围0-90度
    private String range;//观测范围  0或1
    private String savefreq;//数据频率

    public void setCutoffangle(String cutoffangle) {
        this.cutoffangle = cutoffangle;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setSavefreq(String savefreq) {
        this.savefreq = savefreq;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cutoffangle=" + cutoffangle);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(range)) {
            stringBuilder.append("range=" + range);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(savefreq)) {
            stringBuilder.append("savefreq=" + savefreq);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
