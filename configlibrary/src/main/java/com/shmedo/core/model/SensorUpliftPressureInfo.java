package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorUpliftPressureInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 14:15
 * 描述：    传感器类型为扬压力计 15
 */

public class SensorUpliftPressureInfo implements SensorParameter {
    private int triggerThreshold;   //触发阈值 mm
    private double revised;    //修正值m
    private String cordlength;  //绳长m
    private String installationElevation;  //安装高程 m


    public int getTriggerThreshold() {
        return triggerThreshold;
    }


    public void setTriggerThreshold(int triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }


    public double getRevised() {
        return revised;
    }


    public void setRevised(double revised) {
        this.revised = revised;
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


    @Override public String toString() {
        return "SensorUpliftPressureInfo{" +
            "triggerThreshold=" + triggerThreshold +
            ", revised='" + revised + '\'' +
            ", cordlength='" + cordlength + '\'' +
            ", installationElevation='" + installationElevation + '\'' +
            '}';
    }


    @Override public SensorType getSensorType() {
        return SensorType.UPLIFT_PRESSURE_GAUGE;
    }
}
