package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/24/20 <br/>
 * 描述：    生成ADME 基础配置参数拼接指令
 */
public class AdmeBasicConfigEntity implements Validater {
    private String inctype;//测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    private String address;//采集器 / MAC 地址
    private String interdeep;//测斜管孔深
    private String downspeed;//下放速度
    private String downwaitetime;//下放等待时间
    private String datatype;//数据解算方式（0:顶部固定法，1底部固定法）

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
        try {
            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !value.equals("NullKey")) {
                    stringBuilder.append(f.getName());
                    stringBuilder.append("=");
                    stringBuilder.append(value);
                    stringBuilder.append("&");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
