package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/19.
 * 设置采集器采集频度实体类
 */
public class CollectorFrequencyInfo {
    private String type;
    private String timeInterval;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
    }

    @Override
    public String toString() {
        return "CollectorFrequencyInfo{" +
                "type='" + type + '\'' +
                ", timeInterval='" + timeInterval + '\'' +
                '}';
    }
}
