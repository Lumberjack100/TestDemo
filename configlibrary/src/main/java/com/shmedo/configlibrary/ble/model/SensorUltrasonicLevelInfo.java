package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorUltrasonicLevelInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 14:01
 * 描述：    传感器类型为 超声波物位计  06
 */

public class SensorUltrasonicLevelInfo implements SensorParameter, Parcelable {
    private String triggerThreshold;   //触发阈值
    private String correctionValue;     //修正值
    private String probeElevation;  //探头高程

    public SensorUltrasonicLevelInfo() {
    }

    protected SensorUltrasonicLevelInfo(Parcel in) {
        triggerThreshold = in.readString();
        correctionValue = in.readString();
        probeElevation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerThreshold);
        dest.writeString(correctionValue);
        dest.writeString(probeElevation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorUltrasonicLevelInfo> CREATOR = new Creator<SensorUltrasonicLevelInfo>() {
        @Override
        public SensorUltrasonicLevelInfo createFromParcel(Parcel in) {
            return new SensorUltrasonicLevelInfo(in);
        }

        @Override
        public SensorUltrasonicLevelInfo[] newArray(int size) {
            return new SensorUltrasonicLevelInfo[size];
        }
    };

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


    public String getProbeElevation() {
        return probeElevation;
    }


    public void setProbeElevation(String probeElevation) {
        this.probeElevation = probeElevation;
    }


    @Override public SensorType getSensorType() {
        return SensorType.ULTRASONIC_LEVEL_GAUGE;
    }


    @Override
    public String toString() {
        return "SensorUltrasonicLevelInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                ", probeElevation='" + probeElevation + '\'' +
                '}';
    }
}
