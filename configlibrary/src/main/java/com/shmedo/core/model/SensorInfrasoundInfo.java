package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * Created by adu on 2019/11/20.
 * 传感器 次声 21
 */
public class SensorInfrasoundInfo implements SensorParameter {
    private String triggerThreshold;   //触发阈值
    private String correctionValue;     //修正值

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

    @Override
    public String toString() {
        return "SensorInfrasoundInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.INFRASOUND_SENSOR;
    }
}
