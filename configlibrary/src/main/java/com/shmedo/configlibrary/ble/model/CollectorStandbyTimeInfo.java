package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/19.
 *  采集器待机时长实体类
 */
public class CollectorStandbyTimeInfo {
    private String type;
    private String time;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "CollectorStandbyTimeInfo{" +
                "type='" + type + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
