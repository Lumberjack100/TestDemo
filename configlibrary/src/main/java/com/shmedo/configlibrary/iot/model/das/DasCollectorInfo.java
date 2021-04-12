package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  4/12/21 <br/>
 * 描述：     DAS 采集器参数
 */
public class DasCollectorInfo {
    private String type;//采集器型号
    private String addr;//采集器地址
    private String collgap;//采集间隔
    private String calcgap;//解算间隔
    private String standbygap;//待机时长
    private String sensornum;//接入传感器个数

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddr() {
        return TextUtils.isEmpty(addr) ? "" : addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCollgap() {
        return TextUtils.isEmpty(collgap) ? "" : collgap;
    }

    public void setCollgap(String collgap) {
        this.collgap = collgap;
    }

    public String getCalcgap() {
        return TextUtils.isEmpty(calcgap) ? "" : calcgap;
    }

    public void setCalcgap(String calcgap) {
        this.calcgap = calcgap;
    }

    public String getStandbygap() {
        return TextUtils.isEmpty(standbygap) ? "" : standbygap;
    }

    public void setStandbygap(String standbygap) {
        this.standbygap = standbygap;
    }

    public String getSensornum() {
        return TextUtils.isEmpty(sensornum) ? "" : sensornum;
    }

    public void setSensornum(String sensornum) {
        this.sensornum = sensornum;
    }
}
