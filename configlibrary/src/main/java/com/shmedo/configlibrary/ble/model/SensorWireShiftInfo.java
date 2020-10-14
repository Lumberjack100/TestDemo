package com.shmedo.configlibrary.ble.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器拉线位移计 02
 */
public class SensorWireShiftInfo implements SensorParameter, Parcelable {
    private String triggerThreshold;    //触发阈值
    private String correctionValue;     //修正值

    public SensorWireShiftInfo() {

    }

    protected SensorWireShiftInfo(Parcel in) {
        triggerThreshold = in.readString();
        correctionValue = in.readString();
    }

    public static final Creator<SensorWireShiftInfo> CREATOR = new Creator<SensorWireShiftInfo>() {
        @Override
        public SensorWireShiftInfo createFromParcel(Parcel in) {
            return new SensorWireShiftInfo(in);
        }

        @Override
        public SensorWireShiftInfo[] newArray(int size) {
            return new SensorWireShiftInfo[size];
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
    public SensorType getSensorType() {
        return SensorType.WIRE_SHIFT;
    }

    @Override
    public String toString() {
        return "SensorWireShiftInfo{" +
                "triggerThreshold=" + triggerThreshold +
                ", correctionValue=" + correctionValue +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerThreshold);
        dest.writeString(correctionValue);
    }
}
