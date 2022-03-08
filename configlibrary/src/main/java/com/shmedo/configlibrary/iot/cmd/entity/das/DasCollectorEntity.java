package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  4/12/21 <br/>
 * 描述：     生成 DAS 采集器参数拼接指令
 */
public class DasCollectorEntity implements Validater {
    private String type;//采集器型号
    private String addr;//采集器地址
    private String collgap;//采集间隔
    private String calcgap;//解算间隔
    private String standbygap;//待机时长
    private String sensornum;//接入传感器个数

    public void setType(String type) {
        this.type = type;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setCollgap(String collgap) {
        this.collgap = collgap;
    }

    public void setCalcgap(String calcgap) {
        this.calcgap = calcgap;
    }

    public void setStandbygap(String standbygap) {
        this.standbygap = standbygap;
    }

    public void setSensornum(String sensornum) {
        this.sensornum = sensornum;
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
