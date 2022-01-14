package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/1/13 <br/>
 * 描述：     通用数字式传感器信息
 */
public class CommonDigitalSensorInfo implements Parcelable {
    private String triggerThreshold;       //触发阈值
    private String measureLength;          //测段长
    private String correctionValue;        //修正值

    public CommonDigitalSensorInfo() {
    }

    protected CommonDigitalSensorInfo(Parcel in) {
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

    public static final Creator<CommonDigitalSensorInfo> CREATOR = new Creator<CommonDigitalSensorInfo>() {
        @Override
        public CommonDigitalSensorInfo createFromParcel(Parcel in) {
            return new CommonDigitalSensorInfo(in);
        }

        @Override
        public CommonDigitalSensorInfo[] newArray(int size) {
            return new CommonDigitalSensorInfo[size];
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
    public String toString() {
        return "CommonDigitalSensorInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", measureLength='" + measureLength + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }
}
