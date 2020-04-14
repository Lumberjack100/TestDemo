package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/18.
 * 设置雨量计精度的实体类
 */
public class SettingRainPrecisionInfo {

    private int precision;

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    @Override
    public String toString() {
        return "SettingRainPrecisionInfo{" +
                "precision=" + precision +
                '}';
    }
}
