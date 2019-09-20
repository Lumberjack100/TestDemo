package com.shmedo.mcloudapp.entity;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   ProjectDeviceInfo
 * 创建者:   dpc
 * 创建时间:  2019/8/19 14:08
 * 描述：    项目 设备列表
 */
public class ProjectDeviceInfo {

    private int ID;//关联ID
    private String uniqueToken;//唯一标识
    private int deviceID;//设备ID
    private String deviceToken;//设备标识
    private String deviceName;//设备名称
    private int deviceTypeID;//设备类型ID
    private String deviceTypeName;//设备类型名称
    private String deviceStatus;//设备状态
    private String deviceDesc;//设备描述
    private String createTime;//添加时间


    public int getID() {
        return ID;
    }


    public void setID(int ID) {
        this.ID = ID;
    }


    public String getUniqueToken() {
        return uniqueToken;
    }


    public void setUniqueToken(String uniqueToken) {
        this.uniqueToken = uniqueToken;
    }


    public int getDeviceID() {
        return deviceID;
    }


    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }


    public String getDeviceToken() {
        return deviceToken;
    }


    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }


    public String getDeviceName() {
        return deviceName;
    }


    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    public int getDeviceTypeID() {
        return deviceTypeID;
    }


    public void setDeviceTypeID(int deviceTypeID) {
        this.deviceTypeID = deviceTypeID;
    }


    public String getDeviceTypeName() {
        return deviceTypeName;
    }


    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }


    public String getDeviceStatus() {
        return deviceStatus;
    }


    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }


    public String getDeviceDesc() {
        return deviceDesc;
    }


    public void setDeviceDesc(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }


    public String getCreateTime() {
        return createTime;
    }


    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
