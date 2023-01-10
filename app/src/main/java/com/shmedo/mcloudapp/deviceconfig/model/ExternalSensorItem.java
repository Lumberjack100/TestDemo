package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/1/10 <br/>
 * 描述：     TODO
 */
public class ExternalSensorItem {
    private String sensorAddress;
    private int resId = -1;
    private boolean isVibratingWireSensor = false;//是否振弦式传感器


    public ExternalSensorItem() {
    }

    public ExternalSensorItem(String sensorAddress) {
        this.sensorAddress = sensorAddress;
    }

    public ExternalSensorItem(String sensorAddress, int resId) {
        this.sensorAddress = sensorAddress;
        this.resId = resId;
    }

    public ExternalSensorItem(String sensorAddress, int resId, boolean isVibratingWireSensor) {
        this.sensorAddress = sensorAddress;
        this.resId = resId;
        this.isVibratingWireSensor = isVibratingWireSensor;
    }

    public String getSensorAddress() {
        return sensorAddress;
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

    public boolean isVibratingWireSensor() {
        return isVibratingWireSensor;
    }

    public void setVibratingWireSensor(boolean vibratingWireSensor) {
        isVibratingWireSensor = vibratingWireSensor;
    }
}
