package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器测斜仪  04
 */
public class SensorInclinometerInfo implements SensorParameter, Parcelable {
    private String triggerThreshold;       //触发阈值
    private String measureLength;          //测段长
    private String correctionValue;        //修正值

    public SensorInclinometerInfo() {
    }

    protected SensorInclinometerInfo(Parcel in) {
        triggerThreshold = in.readString();
        measureLength = in.readString();
        correctionValue = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerThreshold);
        dest.writeString(measureLength);
        dest.writeString(correctionValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorInclinometerInfo> CREATOR = new Creator<SensorInclinometerInfo>() {
        @Override
        public SensorInclinometerInfo createFromParcel(Parcel in) {
            return new SensorInclinometerInfo(in);
        }

        @Override
        public SensorInclinometerInfo[] newArray(int size) {
            return new SensorInclinometerInfo[size];
        }
    };

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public String getMeasureLength() {
        return measureLength;
    }

    public void setMeasureLength(String measureLength) {
        this.measureLength = measureLength;
    }

    public String getCorrectionValue() {
        return correctionValue;
    }

    public void setCorrectionValue(String correctionValue) {
        this.correctionValue = correctionValue;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.INCLINOMETER;
    }

    @Override
    public String toString() {
        return "SensorInclinometerInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", measureLength='" + measureLength + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }
}
