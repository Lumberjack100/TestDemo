package com.shmedo.core.model;


import com.shmedo.core.enums.SensorType;

/**
 * Created by adu on 2017/12/28.
 * 设置测斜仪测段长实体类（单位MM）（测斜采集器特有参数）
 */
public class SetInclinometerLongInfo {
    private SensorType sensorType;  //传感器类型
    private String measSegment;     //测长

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public String getMeasSegment() {
        return measSegment;
    }

    public void setMeasSegment(String measSegment) {
        this.measSegment = measSegment;
    }

    @Override
    public String toString() {
        return "SetInclinometerLongInfo{" +
                "sensorType=" + sensorType +
                ", measSegment='" + measSegment + '\'' +
                '}';
    }
}
