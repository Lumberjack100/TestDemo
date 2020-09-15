package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.SensorBaudRate;

/**
 * Created by adu on 2017/12/18.
 * 设置传感器口波特率实体类
 */
public class SensorBaudRateInfo {
    private SensorBaudRate sensorBaudRate;

    public SensorBaudRate getSensorBaudRate() {
        return sensorBaudRate;
    }

    public void setSensorBaudRate(SensorBaudRate sensorBaudRate) {
        this.sensorBaudRate = sensorBaudRate;
    }

    @Override
    public String toString() {
        return "SensorBaudRateInfo{" +
                "sensorBaudRate=" + sensorBaudRate +
                '}';
    }
}
