package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   SensorTemperHumidityInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:46
 * 描述：     传感器类型为 温湿度计 12
 */
public class SensorTemperHumidityInfoSub {
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


    @Override public String toString() {
        return "SensorTemperHumidityInfo{" +
            "temperatureTriggerThreshold='" + temperatureTriggerThreshold + '\'' +
            ", humidityTriggerThreshold='" + humidityTriggerThreshold + '\'' +
            '}';
    }
}
