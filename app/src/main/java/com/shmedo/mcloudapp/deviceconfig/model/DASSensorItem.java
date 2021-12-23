package com.shmedo.mcloudapp.deviceconfig.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/12 <br/>
 * 描述：     TODO #gh#
 */
public class DASSensorItem {
    private String sensorAddress;
    private int resId;
    private boolean isAddButton = false;
    private boolean isVibratingWireSensor = false;//是否振弦式传感器


    public DASSensorItem(int resId, boolean isAddButton) {
        this.resId = resId;
        this.isAddButton = isAddButton;
    }

    public DASSensorItem(int resId, boolean isAddButton, String sensorAddress) {
        this.resId = resId;
        this.isAddButton = isAddButton;
        this.sensorAddress = sensorAddress;
    }

    public String getSensorAddress() {
        return TextUtils.isEmpty(sensorAddress) ? "" : sensorAddress;
    }

    public void setSensorAddress(String sensorAddress) {
        this.sensorAddress = sensorAddress;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public boolean isAddButton() {
        return isAddButton;
    }

    public void setAddButton(boolean addButton) {
        isAddButton = addButton;
    }

    public boolean isVibratingWireSensor() {
        return isVibratingWireSensor;
    }

    public void setVibratingWireSensor(boolean vibratingWireSensor) {
        isVibratingWireSensor = vibratingWireSensor;
    }
}
