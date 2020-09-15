package com.shmedo.configlibrary.ble.model;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型为墒情计 08
 */
public class SensorMoistureMeterInfo implements SensorParameter {
    private String humidityTriggerThreshold;   //湿度触发阈值
    private String humidityCorrectionValue;    //湿度修正值
    private String saltTriggerThreshold;       //盐分触发阈值
    private String saltCorrectionValue;        //盐分修正值
    private String temperatureTriggerThreshold;//温度触发阈值
    private String temperatureCorrectionValue; //温度修正值


    public String getHumidityTriggerThreshold() {
        return humidityTriggerThreshold;
    }


    public void setHumidityTriggerThreshold(String humidityTriggerThreshold) {
        this.humidityTriggerThreshold = humidityTriggerThreshold;
    }


    public String getHumidityCorrectionValue() {
        return humidityCorrectionValue;
    }


    public void setHumidityCorrectionValue(String humidityCorrectionValue) {
        this.humidityCorrectionValue = humidityCorrectionValue;
    }


    public String getSaltTriggerThreshold() {
        return saltTriggerThreshold;
    }


    public void setSaltTriggerThreshold(String saltTriggerThreshold) {
        this.saltTriggerThreshold = saltTriggerThreshold;
    }


    public String getSaltCorrectionValue() {
        return saltCorrectionValue;
    }


    public void setSaltCorrectionValue(String saltCorrectionValue) {
        this.saltCorrectionValue = saltCorrectionValue;
    }


    public String getTemperatureTriggerThreshold() {
        return temperatureTriggerThreshold;
    }


    public void setTemperatureTriggerThreshold(String temperatureTriggerThreshold) {
        this.temperatureTriggerThreshold = temperatureTriggerThreshold;
    }


    public String getTemperatureCorrectionValue() {
        return temperatureCorrectionValue;
    }


    public void setTemperatureCorrectionValue(String temperatureCorrectionValue) {
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
