package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/7/21 <br/>
 * 描述：     ADME电机实时运动状态
 */
public class AdmeMotorMotionDataInfo {
    private String pulsenumber;//脉冲数
    private String realmovedistance;//实时运动距离
    private String realmoveangle;// 实时运动角度

    public String getPulsenumber() {
        return TextUtils.isEmpty(pulsenumber) ? "" : pulsenumber;
    }

    public void setPulsenumber(String pulsenumber) {
        this.pulsenumber = pulsenumber;
    }

    public String getRealmovedistance() {
        return TextUtils.isEmpty(realmovedistance) ? "" : realmovedistance;
    }

    public void setRealmovedistance(String realmovedistance) {
        this.realmovedistance = realmovedistance;
    }

    public String getRealmoveangle() {
        return realmoveangle;
    }

    public void setRealmoveangle(String realmoveangle) {
        this.realmoveangle = realmoveangle;
    }
}
