package com.shmedo.configlibrary.iot.model.hac;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/11 <br/>
 * 描述：     ADME AC10 数据测量配置参数
 */
public class HacMeasuringDataInfo {
    private String equipmodel;//电机工作标识 0：停止 1：开始测量 2: 异常
    private String address;//MAC 地址
    private String downwaitetime;//下放等待时间
    private String datatype;//数据解算方式（0:顶部固定法，1底部固定法）
    private String onewaytest;//单向测量 0 :关闭 1:开启
    private List<HacHoleAreaDepthInfo> holelist;
    private String checkreverse;//反转自检


    public String getEquipmodel() {
        return TextUtils.isEmpty(equipmodel) ? "" : equipmodel;
    }

    public void setEquipmodel(String equipmodel) {
        this.equipmodel = equipmodel;
    }

    public String getAddress() {
        return TextUtils.isEmpty(address) ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDownwaitetime() {
        return TextUtils.isEmpty(downwaitetime) ? "" : downwaitetime;
    }

    public void setDownwaitetime(String downwaitetime) {
        this.downwaitetime = downwaitetime;
    }

    public String getDatatype() {
        return TextUtils.isEmpty(datatype) ? "" : datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getOnewaytest() {
        return TextUtils.isEmpty(onewaytest) ? "0" : onewaytest;
    }

    public void setOnewaytest(String onewaytest) {
        this.onewaytest = onewaytest;
    }

    public List<HacHoleAreaDepthInfo> getHolelist() {
        return holelist == null ? new ArrayList<>() : holelist;
    }

    public void setHolelist(List<HacHoleAreaDepthInfo> holelist) {
        this.holelist = holelist;
    }

    public String getCheckreverse() {
        return TextUtils.isEmpty(checkreverse) ? "0" : checkreverse;
    }

    public void setCheckreverse(String checkreverse) {
        this.checkreverse = checkreverse;
    }
}
