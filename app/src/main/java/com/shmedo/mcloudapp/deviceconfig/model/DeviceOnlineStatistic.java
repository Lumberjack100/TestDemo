package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/26 <br/>
 * 描述：     设备在线统计实体
 */
public class DeviceOnlineStatistic {

    /**
     * allCount : 374
     * onlineCount : 52
     * offlineCount : 322
     * onlinePercent : 0.13903743
     * useDevice : 374
     * unUseDevice : 0
     */

    private int allCount;
    private int onlineCount;
    private int offlineCount;
    private double onlinePercent;
    private int useDevice;
    private int unUseDevice;

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
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
}
