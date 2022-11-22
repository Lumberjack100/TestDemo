package com.shmedo.configlibrary.iot.model.e40;

import com.google.gson.annotations.SerializedName;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/24 <br/>
 * 描述：     TODO
 */
public class GPSBean {
    @SerializedName("AZ")
    private double aZ;
    @SerializedName("EL")
    private double eL;
    @SerializedName("L1")
    private int l1;
    @SerializedName("L2")
    private int l2;
    @SerializedName("L3")
    private int l3;

    public double getAZ() {
        return aZ;
    }

    public void setAZ(double aZ) {
        this.aZ = aZ;
    }

    public double getEL() {
        return eL;
    }

    public void setEL(double eL) {
        this.eL = eL;
    }

    public int getL1() {
        return l1;
    }

    public void setL1(int l1) {
        this.l1 = l1;
    }

    public int getL2() {
        return l2;
    }

    public void setL2(int l2) {
        this.l2 = l2;
    }

    public int getL3() {
        return l3;
    }

    public void setL3(int l3) {
        this.l3 = l3;
    }
}
