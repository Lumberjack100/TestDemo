package com.shmedo.core.model;


import com.shmedo.core.interfaces.SensorParameter;
import com.shmedo.core.enums.SensorType;

/**
 * Created by adu on 2017/12/14.
 * 传感器拉线位移计 02
 */
public class SensorWireShiftInfo implements SensorParameter {
    private int triggerThreshold;    //触发阈值
    private double correctionValue;        //修正值

    public int getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(int triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public double getCorrectionValue() {
        return correctionValue;
    }

    public void setCorrectionValue(double correctionValue) {
        this.correctionValue = correctionValue;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.WIRE_SHIFT;
    }

    @Override
    public String toString() {
        return "SensorWireShiftInfo{" +
                "triggerThreshold=" + triggerThreshold +
                ", correctionValue=" + correctionValue +
                '}';
    }
}
