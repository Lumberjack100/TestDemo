package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2019/11/20.
 * 传感器 次声 21
 */
public class SensorInfrasoundInfo implements SensorParameter, Parcelable {
    private String triggerThreshold;   //触发阈值
    private String correctionValue;     //修正值

    public SensorInfrasoundInfo() {
    }

    protected SensorInfrasoundInfo(Parcel in) {
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

    public static final Creator<SensorInfrasoundInfo> CREATOR = new Creator<SensorInfrasoundInfo>() {
        @Override
        public SensorInfrasoundInfo createFromParcel(Parcel in) {
            return new SensorInfrasoundInfo(in);
        }

        @Override
        public SensorInfrasoundInfo[] newArray(int size) {
            return new SensorInfrasoundInfo[size];
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

    @Override
    public String toString() {
        return "SensorInfrasoundInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.INFRASOUND;
    }
}
