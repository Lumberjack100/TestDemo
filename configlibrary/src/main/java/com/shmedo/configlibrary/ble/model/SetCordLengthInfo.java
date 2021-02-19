package com.shmedo.configlibrary.ble.model;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SetCordLengthInfo
 * 创建者:   dpc
 * 创建时间:  2018/4/14 15:00
 * 描述：    TODO #gh#
 */

public class SetCordLengthInfo {
    private double cordLenght;


    public double getCordLenght() {
        return cordLenght;
    }


    public void setCordLenght(double cordLenght) {
        this.cordLenght = cordLenght;
    }


    @Override public String toString() {
        return "SetCordLengthInfo{" +
            "cordLenght=" + cordLenght +
            '}';
    }
}
