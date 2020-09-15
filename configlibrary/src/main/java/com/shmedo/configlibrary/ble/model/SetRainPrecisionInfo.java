package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/18.
 * 设置雨量计精度的实体类
 */
public class SetRainPrecisionInfo {

    private double precision;

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    @Override
    public String toString() {
        return "SettingRainPrecisionInfo{" +
                "precision=" + precision +
                '}';
    }
}
