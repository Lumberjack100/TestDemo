package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/7 <br/>
 * 描述：  传感器为军星轴力计 ZLJ-300T
 */
public class SensorJunXingZljInfo implements SensorParameter {
    private String triggerThreshold;   //触发阈值
    private String sensitivityK;   //灵敏度K
    private String referenceValue; //基准值
    private String manualCorrection;    //手动纠偏

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public String getSensitivityK() {
        return sensitivityK;
    }

    public void setSensitivityK(String sensitivityK) {
        this.sensitivityK = sensitivityK;
    }

    public String getManualCorrection() {
        return manualCorrection;
    }

    public void setManualCorrection(String manualCorrection) {
        this.manualCorrection = manualCorrection;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.JUNXING_ZLJ_300T;
    }

    @Override
    public String toString() {
        return "SensorJunXingZljInfo{" +
                "triggerThreshold=" + triggerThreshold +
                ", sensitivityK=" + sensitivityK +
                ", manualCorrection='" + manualCorrection + '\'' +
                ", referenceValue=" + referenceValue +
                '}';
    }
}
