package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   CollectorSensorParamsInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:31
 * 描述：    XX采集器YY通道的传感器参数
 */
public class CollectorSensorParamsInfoSub<T> {
    private String sensorAddress;   //传感器通道号
    private T sensorData;
    private String sensorType;//传感器类型
    private String collectorModel; //采集器类型
    private String channelNumber;   //通道号

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


    public String getSensorType() {
        return sensorType;
    }


    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getCollectorModel() {
        return collectorModel;
    }

    public void setCollectorModel(String collectorModel) {
        this.collectorModel = collectorModel;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override public String toString() {
        return "CollectorSensorParamsInfoSub{" +
            "sensorAddress='" + sensorAddress + '\'' +
            ", sensorData=" + sensorData +
            ", sensorType='" + sensorType + '\'' +
            ", collectorModel='" + collectorModel + '\'' +
            ", channelNumber='" + channelNumber + '\'' +
            '}';
    }
}
