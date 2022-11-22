package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class MemsBean {
    private String sw;
    private boolean status;
    private double x;
    private double y;
    private double z;

    public String getSw() {
        return TextUtils.isEmpty(sw) ? "" : sw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
