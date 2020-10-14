package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SensorSoilMoistureInfo
 * 创建者:   dpc
 * 创建时间:  2019/5/8 13:59
 * 描述：    传感器类型为 土壤含水率 03
 */

public class SensorSoilMoistureInfo implements SensorParameter, Parcelable {
    private String triggerThreshold;   //触发阈值
    private String correctionValue;     //修正值

    public SensorSoilMoistureInfo() {
    }

    protected SensorSoilMoistureInfo(Parcel in) {
        triggerThreshold = in.readString();
        correctionValue = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerThreshold);
        dest.writeString(correctionValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorSoilMoistureInfo> CREATOR = new Creator<SensorSoilMoistureInfo>() {
        @Override
        public SensorSoilMoistureInfo createFromParcel(Parcel in) {
            return new SensorSoilMoistureInfo(in);
        }

        @Override
        public SensorSoilMoistureInfo[] newArray(int size) {
            return new SensorSoilMoistureInfo[size];
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


    @Override public SensorType getSensorType() {
        return SensorType.SOIL_MOISTURE;
    }


    @Override
    public String toString() {
        return "SensorSoilMoistureInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }
}
