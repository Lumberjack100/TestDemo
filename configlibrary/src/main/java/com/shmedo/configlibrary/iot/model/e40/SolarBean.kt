package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class SolarBean {
    private String sw;
    private boolean status;
    private double sloarvolt;
    private double batvolt;
    private double payloadvolt;

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

    public double getSloarvolt() {
        return sloarvolt;
    }

    public void setSloarvolt(double sloarvolt) {
        this.sloarvolt = sloarvolt;
    }

    public double getBatvolt() {
        return batvolt;
    }

    public void setBatvolt(double batvolt) {
        this.batvolt = batvolt;
    }

    public double getPayloadvolt() {
        return payloadvolt;
    }

    public void setPayloadvolt(double payloadvolt) {
        this.payloadvolt = payloadvolt;
    }
}
