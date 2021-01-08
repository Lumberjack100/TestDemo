package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：      ADME电机实时运动数据
 */
public class AdmeMotorMotionAngleInfo {
    private String pulsenumber;//脉冲数
    private String realmoveangle;// 实时运动角度

    public String getPulsenumber() {
        return TextUtils.isEmpty(pulsenumber) ? "" : pulsenumber;
    }

    public void setPulsenumber(String pulsenumber) {
        this.pulsenumber = pulsenumber;
    }

    public String getRealmoveangle() {
        return realmoveangle;
    }

    public void setRealmoveangle(String realmoveangle) {
        this.realmoveangle = realmoveangle;
    }
}
