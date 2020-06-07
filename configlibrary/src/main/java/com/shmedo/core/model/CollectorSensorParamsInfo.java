package com.shmedo.core.model;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.enums.SensorType;

/**
 * Created by adu on 2017/12/14.
 * XX采集器YY通道的传感器参数
 */
public class CollectorSensorParamsInfo<T> {
    private String sensorAddress;   //传感器通道号
    private T sensorData;
    private SensorType sensorType;//传感器类型
    private CollectorModel collectorModel; //采集器类型
    private String channelNumber;

    public String getSensorAddress() {
        return sensorAddress;
    }

    public void setSensorAddress(String sensorAddress) {
        this.sensorAddress = sensorAddress;
    }

    public T getSensorData() {
        return sensorData;
    }

    public void setSensorData(T sensorData) {
        this.sensorData = sensorData;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public CollectorModel getCollectorModel() {
        return collectorModel;
    }

    public void setCollectorModel(CollectorModel collectorModel) {
        this.collectorModel = collectorModel;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override public String toString() {
        return "CollectorSensorParamsInfo{" +
            "sensorAddress='" + sensorAddress + '\'' +
            ", sensorData=" + sensorData +
            ", sensorType=" + sensorType +
            ", collectorModel=" + collectorModel +
            ", channelNumber='" + channelNumber + '\'' +
            '}';
    }
}
