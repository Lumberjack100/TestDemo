package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：      ADME 电压配置参数
 */
public class AdmeVoltageConfigInfo {
    private String driveovervolt;//驱动器过压阈值
    private String drivelowvolt;//驱动器低压阈值
    private String driveundervolt;//驱动器欠压阈值
    private String inclowvolt;//测斜仪低压阈值
    private String incundervolt;//测斜仪欠压阈值

    public String getDriveovervolt() {
        return TextUtils.isEmpty(driveovervolt) ? "" : driveovervolt;
    }

    public void setDriveovervolt(String driveovervolt) {
        this.driveovervolt = driveovervolt;
    }

    public String getDrivelowvolt() {
        return TextUtils.isEmpty(drivelowvolt) ? "" : drivelowvolt;
    }

    public void setDrivelowvolt(String drivelowvolt) {
        this.drivelowvolt = drivelowvolt;
    }

    public String getDriveundervolt() {
        return TextUtils.isEmpty(driveundervolt) ? "" : driveundervolt;
    }

    public void setDriveundervolt(String driveundervolt) {
        this.driveundervolt = driveundervolt;
    }

    public String getInclowvolt() {
        return TextUtils.isEmpty(inclowvolt) ? "" : inclowvolt;
    }

    public void setInclowvolt(String inclowvolt) {
        this.inclowvolt = inclowvolt;
    }

    public String getIncundervolt() {
        return TextUtils.isEmpty(incundervolt) ? "" : incundervolt;
    }

    public void setIncundervolt(String incundervolt) {
        this.incundervolt = incundervolt;
    }
}
