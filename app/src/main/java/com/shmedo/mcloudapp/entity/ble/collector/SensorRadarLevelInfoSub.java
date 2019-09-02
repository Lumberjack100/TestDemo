package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   SensorRadarLevelInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:44
 * 描述：     传感器类型为 雷达物位计  07
 */
public class SensorRadarLevelInfoSub {
    private String triggerThreshold;   //触发阈值
    private double revised;     //修正值
    private String probeElevation;  //探头高程


    public String getTriggerThreshold() {
        return triggerThreshold;
    }


    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }


    public double getRevised() {
        return revised;
    }


    public void setRevised(double revised) {
        this.revised = revised;
    }


    public String getProbeElevation() {
        return probeElevation;
    }


    public void setProbeElevation(String probeElevation) {
        this.probeElevation = probeElevation;
    }

    @Override public String toString() {
        return "SensorRadarLevelInfo{" +
            "triggerThreshold='" + triggerThreshold + '\'' +
            ", revised='" + revised + '\'' +
            ", probeElevation='" + probeElevation + '\'' +
            '}';
    }
}
