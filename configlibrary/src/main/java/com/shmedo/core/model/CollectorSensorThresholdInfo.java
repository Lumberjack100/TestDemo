package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/19.
 * 设置采集器接入传感器的触发阈值 实体类
 */
public class CollectorSensorThresholdInfo {
    private String type;
    private String threshold;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    @Override
    public String toString() {
        return "CollectorSensorThresholdInfo{" +
                "type='" + type + '\'' +
                ", threshold='" + threshold + '\'' +
                '}';
    }
}
