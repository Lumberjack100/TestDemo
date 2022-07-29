package com.shmedo.configlibrary.iot.model.hac;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/25 <br/>
 * 描述：     HAC 电机实时运动脉冲数据
 */
public class HacMotorMotionDistanceInfo {
    private String pulsenumber;//脉冲数
    private String realmovedistance;//实时运动距离
    private String realholedepth;// 实时测量孔深
    private String abndiasis;// 设备异常诊断

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

    public String getRealholedepth() {
        return TextUtils.isEmpty(realholedepth) ? "" : realholedepth;
    }

    public void setRealholedepth(String realholedepth) {
        this.realholedepth = realholedepth;
    }

    public String getAbndiasis() {
        return TextUtils.isEmpty(abndiasis) ? "0" : abndiasis;
    }

    public void setAbndiasis(String abndiasis) {
        this.abndiasis = abndiasis;
    }
}
