package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/15.
 * 设置心跳包发送间隔
 */
public class HeartbeatSendIntervalInfo {
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return this.time;
    }
}
