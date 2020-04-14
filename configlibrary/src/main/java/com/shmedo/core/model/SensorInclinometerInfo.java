package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器测斜仪  04
 */
public class SensorInclinometerInfo implements SensorParameter {
    private int triggerThreshold;       //触发阈值
    private int measureLength;          //测段长
    private double correctionValue;        //修正值

    public int getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(int triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public int getMeasureLength() {
        return measureLength;
    }

    public void setMeasureLength(int measureLength) {
        this.measureLength = measureLength;
    }

    public double getCorrectionValue() {
        return correctionValue;
    }

    public void setCorrectionValue(double correctionValue) {
        this.correctionValue = correctionValue;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.INCLINOMETER;
    }

    @Override
    public String toString() {
        return "SensorInclinometerInfo{" +
                "triggerThreshold=" + triggerThreshold +
                ", measureLength=" + measureLength +
                ", correctionValue=" + correctionValue +
                '}';
    }
}
