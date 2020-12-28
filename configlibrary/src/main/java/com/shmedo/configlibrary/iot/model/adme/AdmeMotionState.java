package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：     ADME的运行状态
 */
public class AdmeMotionState {
    private String motionstate;//运动状态(0:管口停止，1:管底停止，2:管口测量，3:管口测试，4:上拉测量，5:上拉测试，6:下放测量，7:下放测试)
    private String inctiondis;//测斜仪运动距离

    public String getMotionstate() {
        return TextUtils.isEmpty(motionstate) ? "" : motionstate;
    }

    public void setMotionstate(String motionstate) {
        this.motionstate = motionstate;
    }

    public String getInctiondis() {
        return TextUtils.isEmpty(inctiondis) ? "" : inctiondis;
    }

    public void setInctiondis(String inctiondis) {
        this.inctiondis = inctiondis;
    }
}
