package com.shmedo.configlibrary.ble.model;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorRadarLevelInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 14:13
 * 描述：    传感器类型为 雷达物位计  07
 */

public class SensorRadarLevelInfo implements SensorParameter {
    private String triggerThreshold;   //触发阈值
    private String correctionValue;     //修正值



    public String getTriggerThreshold() {
        return triggerThreshold;
    }


    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }


    public String getCorrectionValue() {
        return correctionValue;
    }


    public void setCorrectionValue(String correctionValue) {
        this.correctionValue = correctionValue;
    }





    @Override public SensorType getSensorType() {
        return SensorType.RADAR_LEVEL_GAUGE;
    }


    @Override
    public String toString() {
        return "SensorRadarLevelInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }
}
