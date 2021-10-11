package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/10/11 <br/>
 * 描述：     气象站 25
 */
public class SensorWeatherStation implements SensorParameter, Parcelable {
    private String triggerThreshold;   //触发阈值
    private String correctionValue;     //修正值

    public SensorWeatherStation() {
    }


    protected SensorWeatherStation(Parcel in) {
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

    public static final Creator<SensorWeatherStation> CREATOR = new Creator<SensorWeatherStation>() {
        @Override
        public SensorWeatherStation createFromParcel(Parcel in) {
            return new SensorWeatherStation(in);
        }

        @Override
        public SensorWeatherStation[] newArray(int size) {
            return new SensorWeatherStation[size];
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
        return SensorType.WEATHER_STATION;
    }

    @Override
    public String toString() {
        return "SensorWeatherStation{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", correctionValue='" + correctionValue + '\'' +
                '}';
    }
}
