package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：    生成ADME 测斜仪配置参数拼接指令
 */
public class AdmeInclinometerEntity implements Validater {
    private String inctype;//测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    private String lowpower;//低功耗模式(0:关闭，1:开启)
    private String address;//采集器 / MAC 地址
    private String collinval;//采集器采集间隔
    private String calcinval;//采集器解算间隔
    private String dormancytime;//休眠时间
    private String interupdate;//测斜仪修正值

    public void setInctype(String inctype) {
        this.inctype = inctype;
    }

    public void setLowpower(String lowpower) {
        this.lowpower = lowpower;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCollinval(String collinval) {
        this.collinval = collinval;
    }

    public void setCalcinval(String calcinval) {
        this.calcinval = calcinval;
    }

    public void setDormancytime(String dormancytime) {
        this.dormancytime = dormancytime;
    }

    public void setInterupdate(String interupdate) {
        this.interupdate = interupdate;
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
