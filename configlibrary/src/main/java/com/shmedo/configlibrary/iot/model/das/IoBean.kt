package com.shmedo.configlibrary.iot.model.das;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：     DAS 状态页面开关量传感器状态
 */
public class IoBean {
    private int type;
    private double vaule;
    private int errno;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getVaule() {
        return vaule;
    }

    public void setVaule(int vaule) {
        this.vaule = vaule;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }
}
