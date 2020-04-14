package com.shmedo.core.model;


import com.shmedo.core.enums.SensorBaudRate;

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
