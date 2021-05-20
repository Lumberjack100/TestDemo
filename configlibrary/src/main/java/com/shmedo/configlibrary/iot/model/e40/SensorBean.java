package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class SensorBean {
    private String sw;
    private boolean status;
    private int addr;
    private int type;
    private double vaule;

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

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getVaule() {
        return vaule;
    }

    public void setVaule(double vaule) {
        this.vaule = vaule;
    }
}
