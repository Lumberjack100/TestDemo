package com.shmedo.configlibrary.iot.cmd.entity.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/24/20 <br/>
 * 描述：    生成ADME 基础配置参数拼接指令
 */
public class AdmeBasicConfigParamEntity implements Validater {
    private String inctype;//测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    private String address;//采集器 / MAC 地址
    private String interdeep;//测斜管孔深
    private String downspeed;//下放速度
    private String downwaitetime;//下放等待时间
    private String datatype;//数据结算方式（0:顶固定法，1底固定法）

    public void setInctype(String inctype) {
        this.inctype = inctype;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setInterdeep(String interdeep) {
        this.interdeep = interdeep;
    }

    public void setDownspeed(String downspeed) {
        this.downspeed = downspeed;
    }

    public void setDownwaitetime(String downwaitetime) {
        this.downwaitetime = downwaitetime;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("inctype=" + inctype);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(address)) {
            stringBuilder.append("address=" + address);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(interdeep)) {
            stringBuilder.append("interdeep=" + interdeep);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downspeed)) {
            stringBuilder.append("downspeed=" + downspeed);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downwaitetime)) {
            stringBuilder.append("downwaitetime=" + downwaitetime);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(datatype)) {
            stringBuilder.append("datatype=" + datatype);
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
