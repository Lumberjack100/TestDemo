package com.shmedo.configlibrary.ble.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/6/20 <br/>
 * 描述：     Das 倾角计信息 X、Y、Z 轴角度、加速度
 */
public class InclinometerInfo {
    private String status;
    private String xAxis;
    private String yAxis;
    private String zAxis;
    private String xAcceleration;
    private String yAcceleration;
    private String zAcceleration;


    public String getStatus() {
        return TextUtils.isEmpty(status) ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getxAxis() {
        return TextUtils.isEmpty(xAxis) ? "" : xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public String getyAxis() {
        return TextUtils.isEmpty(yAxis) ? "" : yAxis;
    }

    public void setyAxis(String yAxis) {
        this.yAxis = yAxis;
    }

    public String getzAxis() {
        return TextUtils.isEmpty(zAxis) ? "" : zAxis;
    }

    public void setzAxis(String zAxis) {
        this.zAxis = zAxis;
    }

    public String getxAcceleration() {
        return TextUtils.isEmpty(xAcceleration) ? "" : xAcceleration;
    }

    public void setxAcceleration(String xAcceleration) {
        this.xAcceleration = xAcceleration;
    }

    public String getyAcceleration() {
        return TextUtils.isEmpty(yAcceleration) ? "" : yAcceleration;
    }

    public void setyAcceleration(String yAcceleration) {
        this.yAcceleration = yAcceleration;
    }

    public String getzAcceleration() {
        return TextUtils.isEmpty(zAcceleration) ? "" : zAcceleration;
    }

    public void setzAcceleration(String zAcceleration) {
        this.zAcceleration = zAcceleration;
    }
}
