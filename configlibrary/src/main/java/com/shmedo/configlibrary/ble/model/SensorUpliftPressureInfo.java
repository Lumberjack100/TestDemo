package com.shmedo.configlibrary.ble.model;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorUpliftPressureInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 14:15
 * 描述：    传感器类型为扬压力计 15
 */

public class SensorUpliftPressureInfo implements SensorParameter {
    private String triggerThreshold;   //触发阈值 mm
    private String correctionValue;    //修正值m
    private String cordlength;  //绳长m
    private String installationElevation;  //安装高程 m


    public String getTriggerThreshold() {
        return triggerThreshold;
    }


    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }


    public String getCorrectionValue() {
        return correctionValue;
    }


    public void setCorrectionValue(String correctionValue) {
        this.correctionValue = correctionValue;
    }


    public String getCordlength() {
        return cordlength;
    }


    public void setCordlength(String cordlength) {
        this.cordlength = cordlength;
    }


    public String getInstallationElevation() {
        return installationElevation;
    }


    public void setInstallationElevation(String installationElevation) {
        this.installationElevation = installationElevation;
    }


    @Override
    public String toString() {
        return "SensorUpliftPressureInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                ", cordlength='" + cordlength + '\'' +
                ", installationElevation='" + installationElevation + '\'' +
                '}';
    }

    @Override public SensorType getSensorType() {
        return SensorType.UPLIFT_PRESSURE_GAUGE;
    }
}
