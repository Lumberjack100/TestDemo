package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorSoilMoistureInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 13:59
 * 描述：    传感器类型为 土壤含水率 03
 */

public class SensorSoilMoistureInfo implements SensorParameter {
    private String triggerThreshold;   //触发阈值
    private double revised;     //修正值


    public String getTriggerThreshold() {
        return triggerThreshold;
    }


    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }


    public double getRevised() {
        return revised;
    }


    public void setRevised(double revised) {
        this.revised = revised;
    }


    @Override public SensorType getSensorType() {
        return SensorType.SOIL_MOISTURE;
    }


    @Override public String toString() {
        return "SensorSoilMoistureInfo{" +
            "triggerThreshold='" + triggerThreshold + '\'' +
            ", revised='" + revised + '\'' +
            '}';
    }
}
