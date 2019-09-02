package com.shmedo.mcloudapp.entity.event;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.event
 * 文件名:   MapDeviceEvent
 * 创建者:   dpc
 * 创建时间:  2019/7/30 14:25
 * 描述：
 */
public class MapDeviceEvent {
    private String type;
    private String deviceName;


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getDeviceName() {
        return deviceName;
    }


    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
