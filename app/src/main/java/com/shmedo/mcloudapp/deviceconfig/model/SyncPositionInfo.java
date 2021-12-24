package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 创建者:   dpc
 * 创建时间:  2019-12-25
 * 描述：    同步位置实体类
 */
public class SyncPositionInfo {
    private double latitude;//纬度
    private double longitude;//经度
    private String address;
    private String type;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SyncPositionBean{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", address='" + address + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
