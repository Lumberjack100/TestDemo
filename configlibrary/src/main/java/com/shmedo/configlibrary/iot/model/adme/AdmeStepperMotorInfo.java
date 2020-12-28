package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：    ADME 步进电机参数
 */
public class AdmeStepperMotorInfo {
    private String posnegtest;//正反测（0:关闭，1:开启）
    private String absprsion;//绝对精度修正值
    private String movspeed;//电机运动速度

    public String getPosnegtest() {
        return TextUtils.isEmpty(posnegtest) ? "" : posnegtest;
    }

    public void setPosnegtest(String posnegtest) {
        this.posnegtest = posnegtest;
    }

    public String getAbsprsion() {
        return TextUtils.isEmpty(absprsion) ? "" : absprsion;
    }

    public void setAbsprsion(String absprsion) {
        this.absprsion = absprsion;
    }

    public String getMovspeed() {
        return TextUtils.isEmpty(movspeed) ? "" : movspeed;
    }

    public void setMovspeed(String movspeed) {
        this.movspeed = movspeed;
    }
}
