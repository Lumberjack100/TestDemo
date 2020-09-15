package com.shmedo.configlibrary.ble.model;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorTemperHumidityInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 14:14
 * 描述：    传感器类型为 温湿度计 12
 */

public class SensorTemperHumidityInfo implements SensorParameter {
    private String temperatureTriggerThreshold; //温度触发阈值 ℃
    private String humidityTriggerThreshold; //湿度触发阈值 RH%


    public String getTemperatureTriggerThreshold() {
        return temperatureTriggerThreshold;
    }


    public void setTemperatureTriggerThreshold(String temperatureTriggerThreshold) {
        this.temperatureTriggerThreshold = temperatureTriggerThreshold;
    }


    public String getHumidityTriggerThreshold() {
        return humidityTriggerThreshold;
    }


    public void setHumidityTriggerThreshold(String humidityTriggerThreshold) {
        this.humidityTriggerThreshold = humidityTriggerThreshold;
    }


    @Override public SensorType getSensorType() {
        return SensorType.TEMPERATURE_HUMIDITY_METER;
    }


    @Override public String toString() {
        return "SensorTemperHumidityInfo{" +
            "temperatureTriggerThreshold='" + temperatureTriggerThreshold + '\'' +
            ", humidityTriggerThreshold='" + humidityTriggerThreshold + '\'' +
            '}';
    }
}
