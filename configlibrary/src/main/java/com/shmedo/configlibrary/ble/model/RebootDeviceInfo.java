package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/15.
 * 重启设备的实体类
 */
public class RebootDeviceInfo {
    private int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "RebootDeviceInfo{" +
                "time=" + time +
                '}';
    }
}
