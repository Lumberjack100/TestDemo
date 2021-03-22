package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/22/21 <br/>
 * 描述：     压电式雨量计 01
 */
public class SensorPiezoelectricRainGauge implements SensorParameter, Parcelable {
    private String triggerThreshold;    //触发阈值
    private String correctionValue;     //修正值

    public SensorPiezoelectricRainGauge() {
    }

    protected SensorPiezoelectricRainGauge(Parcel in) {
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

    public static final Creator<SensorPiezoelectricRainGauge> CREATOR = new Creator<SensorPiezoelectricRainGauge>() {
        @Override
        public SensorPiezoelectricRainGauge createFromParcel(Parcel in) {
            return new SensorPiezoelectricRainGauge(in);
        }

        @Override
        public SensorPiezoelectricRainGauge[] newArray(int size) {
            return new SensorPiezoelectricRainGauge[size];
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
        return SensorType.RAIN_GAUGE;
    }

    @Override
    public String toString() {
        return "SensorPiezoelectricRainGauge{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }
}
