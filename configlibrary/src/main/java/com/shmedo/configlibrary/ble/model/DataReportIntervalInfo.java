package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/19.
 * 设置数据上报间隔实体类
 */
public class DataReportIntervalInfo {
    private int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "DataReportIntervalInfo{" +
                "time=" + time +
                '}';
    }
}
