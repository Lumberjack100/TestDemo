package com.shmedo.mcloudapp.projects.model;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   LocationResult
 * 创建者:   dpc
 * 创建时间:  2019/8/6 19:33
 * 描述：    位置
 */
public class LocationResult {
    private double lng;
    private double lat;


    public double getLng() {
        return lng;
    }


    public void setLng(double lng) {
        this.lng = lng;
    }


    public double getLat() {
        return lat;
    }


    public void setLat(double lat) {
        this.lat = lat;
    }


    @Override public String toString() {
        return "LocationResult{" +
            "lng=" + lng +
            ", lat=" + lat +
            '}';
    }
}
