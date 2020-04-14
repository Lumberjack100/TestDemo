package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorUltrasonicLevelInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 14:01
 * 描述：    传感器类型为 超声波物位计  06
 */

public class SensorUltrasonicLevelInfo implements SensorParameter {
    private String triggerThreshold;   //触发阈值
    private double revised;     //修正值
    private String probeElevation;  //探头高程


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


    public String getProbeElevation() {
        return probeElevation;
    }


    public void setProbeElevation(String probeElevation) {
        this.probeElevation = probeElevation;
    }


    @Override public SensorType getSensorType() {
        return SensorType.ULTRASONIC_LEVEL_GAUGE;
    }


    @Override public String toString() {
        return "SensorUltrasonicLevelInfo{" +
            "triggerThreshold='" + triggerThreshold + '\'' +
            ", revised='" + revised + '\'' +
            ", probeElevation='" + probeElevation + '\'' +
            '}';
    }
}
