package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/23 <br/>
 * 描述：     设备在线统计实体
 */
public class DeviceStatisticInfo {
    private int totalCount;
    private int onlineCount;
    private int offlineCount;
    private int usableCount;
    private int unusableCount;
    private double onlinePercent;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
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

    public int getUsableCount() {
        return usableCount;
    }

    public void setUsableCount(int usableCount) {
        this.usableCount = usableCount;
    }

    public int getUnusableCount() {
        return unusableCount;
    }

    public void setUnusableCount(int unusableCount) {
        this.unusableCount = unusableCount;
    }

    public double getOnlinePercent() {
        return onlinePercent;
    }

    public void setOnlinePercent(double onlinePercent) {
        this.onlinePercent = onlinePercent;
    }
}
