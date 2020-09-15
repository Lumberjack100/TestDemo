package com.shmedo.configlibrary.ble.model;

import com.shmedo.configlibrary.ble.enums.CollectorModel;


/**
 * Created by adu on 2017/12/21.
 * 设置采集器接入传感器修正值 实体类
 */
public class CollectorSensorRevisedInfo {
    private CollectorModel collectorModel;
    private String address;
    private String humidity;    //湿度
    private String salinity;    //盐分
    private String temperature; //温度

    public CollectorModel getCollectorModel() {
        return collectorModel;
    }

    public void setCollectorModel(CollectorModel collectorModel) {
        this.collectorModel = collectorModel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getSalinity() {
        return salinity;
    }

    public void setSalinity(String salinity) {
        this.salinity = salinity;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "CollectorSensorRevisedInfo{" +
                "collectorModel=" + collectorModel +
                ", address='" + address + '\'' +
                ", humidity='" + humidity + '\'' +
                ", salinity='" + salinity + '\'' +
                ", temperature='" + temperature + '\'' +
                '}';
    }
}
