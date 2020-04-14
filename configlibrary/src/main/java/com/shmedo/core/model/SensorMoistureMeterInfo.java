package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型为墒情计 08
 */
public class SensorMoistureMeterInfo implements SensorParameter {
    private int humidityTriggerThreshold;   //湿度触发阈值
    private double humidityCorrectionValue;    //湿度修正值
    private int saltTriggerThreshold;       //盐分触发阈值
    private double saltCorrectionValue;        //盐分修正值
    private int temperatureTriggerThreshold;//温度触发阈值
    private double temperatureCorrectionValue; //温度修正值


    public int getHumidityTriggerThreshold() {
        return humidityTriggerThreshold;
    }


    public void setHumidityTriggerThreshold(int humidityTriggerThreshold) {
        this.humidityTriggerThreshold = humidityTriggerThreshold;
    }


    public double getHumidityCorrectionValue() {
        return humidityCorrectionValue;
    }


    public void setHumidityCorrectionValue(double humidityCorrectionValue) {
        this.humidityCorrectionValue = humidityCorrectionValue;
    }


    public int getSaltTriggerThreshold() {
        return saltTriggerThreshold;
    }


    public void setSaltTriggerThreshold(int saltTriggerThreshold) {
        this.saltTriggerThreshold = saltTriggerThreshold;
    }


    public double getSaltCorrectionValue() {
        return saltCorrectionValue;
    }


    public void setSaltCorrectionValue(double saltCorrectionValue) {
        this.saltCorrectionValue = saltCorrectionValue;
    }


    public int getTemperatureTriggerThreshold() {
        return temperatureTriggerThreshold;
    }


    public void setTemperatureTriggerThreshold(int temperatureTriggerThreshold) {
        this.temperatureTriggerThreshold = temperatureTriggerThreshold;
    }


    public double getTemperatureCorrectionValue() {
        return temperatureCorrectionValue;
    }


    public void setTemperatureCorrectionValue(double temperatureCorrectionValue) {
        this.temperatureCorrectionValue = temperatureCorrectionValue;
    }


    @Override
    public SensorType getSensorType() {
        return SensorType.MOISTURE_METER;
    }

    @Override
    public String toString() {
        return "SensorMoistureMeterInfo{" +
                "humidityTriggerThreshold=" + humidityTriggerThreshold +
                ", humidityCorrectionValue=" + humidityCorrectionValue +
                ", saltTriggerThreshold=" + saltTriggerThreshold +
                ", saltCorrectionValue=" + saltCorrectionValue +
                ", temperatureTriggerThreshold=" + temperatureTriggerThreshold +
                ", temperatureCorrectionValue=" + temperatureCorrectionValue +
                '}';
    }
}
