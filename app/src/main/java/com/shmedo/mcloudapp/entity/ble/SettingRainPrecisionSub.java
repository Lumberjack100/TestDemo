package com.shmedo.mcloudapp.entity.ble;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble
 * 文件名:   SettingRainPrecisionSub
 * 创建者:   dpc
 * 创建时间:  2019/4/24 17:16
 * 描述：    雨量站开关
 */
public class SettingRainPrecisionSub {
    private int precision;

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    @Override
    public String toString() {
        return "SettingRainPrecisionSub{" +
            "precision=" + precision +
            '}';
    }
}
