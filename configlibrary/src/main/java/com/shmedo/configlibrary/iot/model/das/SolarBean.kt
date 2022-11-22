package com.shmedo.configlibrary.iot.model.das;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：     DAS 状态页面太阳能控制器状态
 */
public class SolarBean {
    private int errno;//错误码
    private double solarvolt;//太阳能板电压
    private double batvolt;//蓄电池电压
    private double solarpwr;//太阳能板功率
    private double loadpwr;//负载功率

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public double getSolarvolt() {
        return solarvolt;
    }

    public void setSolarvolt(double solarvolt) {
        this.solarvolt = solarvolt;
    }

    public double getBatvolt() {
        return batvolt;
    }

    public void setBatvolt(double batvolt) {
        this.batvolt = batvolt;
    }

    public double getSolarpwr() {
        return solarpwr;
    }

    public void setSolarpwr(double solarpwr) {
        this.solarpwr = solarpwr;
    }

    public double getLoadpwr() {
        return loadpwr;
    }

    public void setLoadpwr(double loadpwr) {
        this.loadpwr = loadpwr;
    }
}
