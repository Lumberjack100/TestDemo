package com.shmedo.configlibrary.iot.cmd.entity.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：     生成板卡解算参数拼接指令
 */
public class E40BoardSolutionEntity implements Validater {
    private String inittime;//初始化时间
    private String calcgap;//解算时间
    private String smoothlevel;//平滑等级
    private String reinit;//重新初始化
    private String rtkdynamicmode;//RTK动态模式
    private String corrval;//形变修正值

    public void setInittime(String inittime) {
        this.inittime = inittime;
    }

    public void setCalcgap(String calcgap) {
        this.calcgap = calcgap;
    }

    public void setSmoothlevel(String smoothlevel) {
        this.smoothlevel = smoothlevel;
    }

    public void setReinit(String reinit) {
        this.reinit = reinit;
    }

    public void setRtkdynamicmode(String rtkdynamicmode) {
        this.rtkdynamicmode = rtkdynamicmode;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("inittime=" + inittime);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(calcgap)) {
            stringBuilder.append("calcgap=" + calcgap);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(smoothlevel)) {
            stringBuilder.append("smoothlevel=" + smoothlevel);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(reinit)) {
            stringBuilder.append("reinit=" + reinit);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(rtkdynamicmode)) {
            stringBuilder.append("rtkdynamicmode=" + rtkdynamicmode);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(corrval)) {
            stringBuilder.append("corrval=" + corrval);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
