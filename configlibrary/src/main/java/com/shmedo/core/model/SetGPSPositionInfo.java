package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/18.
 * 设置GPS定位
 */
public class SetGPSPositionInfo {
    private String sensitivity;
    private int accuracy;

    public String getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return "SettingGPSPositionInfo{" +
                "sensitivity='" + sensitivity + '\'' +
                ", accuracy=" + accuracy +
                '}';
    }
}
