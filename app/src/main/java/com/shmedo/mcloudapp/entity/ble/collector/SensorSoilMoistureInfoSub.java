package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   SensorSoilMoistureInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:45
 * 描述：    传感器类型为 土壤含水率 03
 */
public class SensorSoilMoistureInfoSub {
    private String triggerThreshold;   //触发阈值
    private double revised;     //修正值


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

    @Override public String toString() {
        return "SensorSoilMoistureInfo{" +
            "triggerThreshold='" + triggerThreshold + '\'' +
            ", revised='" + revised + '\'' +
            '}';
    }
}
