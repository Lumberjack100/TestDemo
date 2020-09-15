package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器拉线位移计 02
 */
public class SensorWireShiftInfo implements SensorParameter {
    private String triggerThreshold;    //触发阈值
    private String correctionValue;        //修正值

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
