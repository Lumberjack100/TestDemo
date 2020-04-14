package com.shmedo.core.model;

/**
 * Created by adu on 2018/1/4.
 *  设置GPRS持续在线时长
 */
public class SetGPRSOnlineTimeInfo {
    private int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "SetGPRSOnlineTimeInfo{" +
                "time=" + time +
                '}';
    }
}
