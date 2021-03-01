package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：     E40 板卡解算参数信息
 */
public class E40BoardSolutionInfo {
    private String inittime;//初始化时间
    private String calcgap;//解算时间
    private String smoothlevel;//平滑等级
    private String reinit;//重新初始化
    private String rtkdynamicmode;//RTK动态模式
    private String corrval;//形变修正值

    public String getInittime() {
        return TextUtils.isEmpty(inittime) ? "" : inittime;
    }

    public void setInittime(String inittime) {
        this.inittime = inittime;
    }

    public String getCalcgap() {
        return TextUtils.isEmpty(calcgap) ? "" : calcgap;
    }

    public void setCalcgap(String calcgap) {
        this.calcgap = calcgap;
    }

    public String getSmoothlevel() {
        return TextUtils.isEmpty(smoothlevel) ? "" : smoothlevel;
    }

    public void setSmoothlevel(String smoothlevel) {
        this.smoothlevel = smoothlevel;
    }

    public String getReinit() {
        return TextUtils.isEmpty(reinit) ? "" : reinit;
    }

    public void setReinit(String reinit) {
        this.reinit = reinit;
    }

    public String getRtkdynamicmode() {
        return TextUtils.isEmpty(rtkdynamicmode) ? "" : rtkdynamicmode;
    }

    public void setRtkdynamicmode(String rtkdynamicmode) {
        this.rtkdynamicmode = rtkdynamicmode;
    }

    public String getCorrval() {
        return TextUtils.isEmpty(corrval) ? "" : corrval;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }
}
