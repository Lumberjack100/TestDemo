package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/26 <br/>
 * 描述：   不同类型设备在线统计实体
 */
public class DeviceOnlineTypeStatistic {


    /**
     * deviceTypeID : 4
     * deviceTypeName : DAS
     * allCount : 165
     * useDevice : 165
     * unUseDevice : 0
     * onlineCount : 14
     * offlineCount : 151
     * onlinePercent : 0.084848486
     */

    private int deviceTypeID;
    private String deviceTypeName;
    private int allCount;
    private int useDevice;
    private int unUseDevice;
    private int onlineCount;
    private int offlineCount;
    private double onlinePercent;

    private boolean isChecked = false;

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

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public int getUseDevice() {
        return useDevice;
    }

    public void setUseDevice(int useDevice) {
        this.useDevice = useDevice;
    }

    public int getUnUseDevice() {
        return unUseDevice;
    }

    public void setUnUseDevice(int unUseDevice) {
        this.unUseDevice = unUseDevice;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public int getOfflineCount() {
        return offlineCount;
    }

    public void setOfflineCount(int offlineCount) {
        this.offlineCount = offlineCount;
    }

    public double getOnlinePercent() {
        return onlinePercent;
    }

    public void setOnlinePercent(double onlinePercent) {
        this.onlinePercent = onlinePercent;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
