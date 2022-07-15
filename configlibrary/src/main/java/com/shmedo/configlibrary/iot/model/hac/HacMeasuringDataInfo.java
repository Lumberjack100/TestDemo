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
    private String address;//MAC 地址
    private String downwaitetime;//下放等待时间
    private String datatype;//数据结算方式（0:顶固定法，1底固定法）
    private String onewaytest;//单向测量
    private List<HacHoleAreaDepthInfo> holelist;

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
}
