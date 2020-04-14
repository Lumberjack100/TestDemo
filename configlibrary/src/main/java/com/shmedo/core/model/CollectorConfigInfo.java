package com.shmedo.core.model;

import java.io.Serializable;

/**
 * Created by adu on 2017/12/11.
 * 采集器配置
 */
public class CollectorConfigInfo implements Serializable {
    private String collectorAddress;    //采集器地址
    private String standbyTime;         //待机时长
    private String workTime;            //工作时长
    private String collectorInterval;   //采集间隔
    private int accessSum;              //接入总数

    public CollectorConfigInfo() {
    }

    public CollectorConfigInfo(String collectorAddress, String standbyTime, String workTime, String collectorInterval, int accessSum) {
        this.collectorAddress = collectorAddress;
        this.standbyTime = standbyTime;
        this.workTime = workTime;
        this.collectorInterval = collectorInterval;
        this.accessSum = accessSum;
    }

    public String getCollectorAddress() {
        return collectorAddress;
    }

    public void setCollectorAddress(String collectorAddress) {
        this.collectorAddress = collectorAddress;
    }

    public String getStandbyTime() {
        return standbyTime;
    }

    public void setStandbyTime(String standbyTime) {
        this.standbyTime = standbyTime;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }

    public String getCollectorInterval() {
        return collectorInterval;
    }

    public void setCollectorInterval(String collectorInterval) {
        this.collectorInterval = collectorInterval;
    }

    public int getAccessSum() {
        return accessSum;
    }

    public void setAccessSum(int accessSum) {
        this.accessSum = accessSum;
    }

    @Override
    public String toString() {
        return "CollectorConfigInfo{" +
                "collectorAddress='" + collectorAddress + '\'' +
                ", standbyTime='" + standbyTime + '\'' +
                ", workTime='" + workTime + '\'' +
                ", collectorInterval='" + collectorInterval + '\'' +
                ", accessSum=" + accessSum +
                '}';
    }
}
