package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器测斜仪  04
 */
public class SensorInclinometerInfo implements SensorParameter {
    private String triggerThreshold;       //触发阈值
    private String measureLength;          //测段长
    private String correctionValue;        //修正值

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public String getMeasureLength() {
        return measureLength;
    }

    public void setMeasureLength(String measureLength) {
        this.measureLength = measureLength;
    }

    public String getCorrectionValue() {
        return correctionValue;
    }

    public void setCorrectionValue(String correctionValue) {
        this.correctionValue = correctionValue;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.INCLINOMETER;
    }

    @Override
    public String toString() {
        return "SensorInclinometerInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", measureLength='" + measureLength + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }
}
