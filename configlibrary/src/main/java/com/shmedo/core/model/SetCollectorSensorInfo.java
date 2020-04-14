package com.shmedo.core.model;


import com.shmedo.core.enums.CollectorModel;

/**
 * Created by adu on 2017/12/19.
 * 设置采集器接入的传感器实体类
 */
public class SetCollectorSensorInfo {
    private CollectorModel number;
    private String sensorNumber;
    private String addressType;

    public CollectorModel getNumber() {
        return number;
    }

    public void setNumber(CollectorModel number) {
        this.number = number;
    }

    public String getSensorNumber() {
        return sensorNumber;
    }

    public void setSensorNumber(String sensorNumber) {
        this.sensorNumber = sensorNumber;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    @Override
    public String toString() {
        return "SetCollectorSensorInfo{" +
                "number=" + number +
                ", sensorNumber='" + sensorNumber + '\'' +
                ", addressType='" + addressType + '\'' +
                '}';
    }
}
