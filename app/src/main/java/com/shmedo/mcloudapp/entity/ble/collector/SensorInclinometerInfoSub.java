package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   SensorInclinometerInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:42
 * 描述：    传感器测斜仪  04
 */
public class SensorInclinometerInfoSub {
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
    public String toString() {
        return "SensorInclinometerInfo{" +
            "triggerThreshold=" + triggerThreshold +
            ", measureLength=" + measureLength +
            ", correctionValue=" + correctionValue +
            '}';
    }
}
