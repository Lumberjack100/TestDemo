package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   SensorWireShiftInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:51
 * 描述：    传感器拉线位移计 02
 */
public class SensorWireShiftInfoSub {
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
    public String toString() {
        return "SensorWireShiftInfo{" +
            "triggerThreshold=" + triggerThreshold +
            ", correctionValue=" + correctionValue +
            '}';
    }
}
