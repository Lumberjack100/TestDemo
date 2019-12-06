package com.shmedo.mcloudapp.entity;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   DeviceSensorResult
 * 创建者:   dpc
 * 创建时间:  2019/3/28 11:31
 *
 */
public class DeviceSensorResult {
   private Integer sensorID;    //传感器ID
   private String sensorName;   //触感器名称
   private String sensorAlias;  //传感器别名
   private Integer sensorType;  //传感器类型
   private String sensorTypeName;   //传感器类型名称


    public Integer getSensorID() {
        return sensorID;
    }


    public void setSensorID(Integer sensorID) {
        this.sensorID = sensorID;
    }


    public String getSensorName() {
        return sensorName;
    }


    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }


    public String getSensorAlias() {
        return sensorAlias;
    }


    public void setSensorAlias(String sensorAlias) {
        this.sensorAlias = sensorAlias;
    }


    public Integer getSensorType() {
        return sensorType;
    }


    public void setSensorType(Integer sensorType) {
        this.sensorType = sensorType;
    }


    public String getSensorTypeName() {
        return sensorTypeName;
    }


    public void setSensorTypeName(String sensorTypeName) {
        this.sensorTypeName = sensorTypeName;
    }
}
