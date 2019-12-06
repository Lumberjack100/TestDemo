package com.shmedo.mcloudapp.entity.parameter;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   BaseSensorParameter
 * 创建者:   dpc
 * 创建时间:  2019/6/27 14:05
 *
 */
public class BaseSensorParameter {
    private String sensorType;
    private String sensorAddress;


    public String getSensorType() {
        return sensorType;
    }


    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }


    public String getSensorAddress() {
        return sensorAddress;
    }


    public void setSensorAddress(String sensorAddress) {
        this.sensorAddress = sensorAddress;
    }
}
