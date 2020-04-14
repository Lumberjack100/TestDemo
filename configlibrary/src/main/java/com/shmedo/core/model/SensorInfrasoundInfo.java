package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * Created by adu on 2019/11/20.
 * 传感器 次声 21
 */
public class SensorInfrasoundInfo implements SensorParameter {
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

    @Override
    public String toString() {
        return "SensorInfrasoundInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", revised=" + revised +
                '}';
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.INFRASOUND_SENSOR;
    }
}
