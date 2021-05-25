package com.shmedo.configlibrary.iot.model.e40;

import com.google.gson.annotations.SerializedName;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/24 <br/>
 * 描述：     TODO
 */
public class BDSBean {
    @SerializedName("SAT")
    private String sAT;
    @SerializedName("AZ")
    private Integer aZ;
    @SerializedName("EL")
    private Integer eL;
    @SerializedName("L1")
    private Integer l1;
    @SerializedName("L2")
    private Integer l2;
    @SerializedName("L3")
    private Integer l3;

    public String getSAT() {
        return sAT;
    }

    public void setSAT(String sAT) {
        this.sAT = sAT;
    }

    public Integer getAZ() {
        return aZ;
    }

    public void setAZ(Integer aZ) {
        this.aZ = aZ;
    }

    public Integer getEL() {
        return eL;
    }

    public void setEL(Integer eL) {
        this.eL = eL;
    }

    public Integer getL1() {
        return l1;
    }

    public void setL1(Integer l1) {
        this.l1 = l1;
    }

    public Integer getL2() {
        return l2;
    }

    public void setL2(Integer l2) {
        this.l2 = l2;
    }

    public Integer getL3() {
        return l3;
    }

    public void setL3(Integer l3) {
        this.l3 = l3;
    }
}
