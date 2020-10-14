package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/7 <br/>
 * 描述：  传感器为轴力计 ZLJ-300T
 */
public class SensorJunXingZljInfo implements SensorParameter, Parcelable {
    private String triggerThreshold;   //触发阈值
    private String polynomialRatioA;//标定系数A
    private String temperatureCoefficientB;//温度系数b
    private String referenceValue; //基准值
    private String CreateTemperature;   //初始化温度T0
    private String manualCorrection;    //手动纠偏

    public SensorJunXingZljInfo() {
    }

    protected SensorJunXingZljInfo(Parcel in) {
        triggerThreshold = in.readString();
        polynomialRatioA = in.readString();
        temperatureCoefficientB = in.readString();
        referenceValue = in.readString();
        CreateTemperature = in.readString();
        manualCorrection = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerThreshold);
        dest.writeString(polynomialRatioA);
        dest.writeString(temperatureCoefficientB);
        dest.writeString(referenceValue);
        dest.writeString(CreateTemperature);
        dest.writeString(manualCorrection);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorJunXingZljInfo> CREATOR = new Creator<SensorJunXingZljInfo>() {
        @Override
        public SensorJunXingZljInfo createFromParcel(Parcel in) {
            return new SensorJunXingZljInfo(in);
        }

        @Override
        public SensorJunXingZljInfo[] newArray(int size) {
            return new SensorJunXingZljInfo[size];
        }
    };

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public String getPolynomialRatioA() {
        return polynomialRatioA;
    }

    public void setPolynomialRatioA(String polynomialRatioA) {
        this.polynomialRatioA = polynomialRatioA;
    }

    public String getTemperatureCoefficientB() {
        return temperatureCoefficientB;
    }

    public void setTemperatureCoefficientB(String temperatureCoefficientB) {
        this.temperatureCoefficientB = temperatureCoefficientB;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    public String getCreateTemperature() {
        return CreateTemperature;
    }

    public void setCreateTemperature(String createTemperature) {
        CreateTemperature = createTemperature;
    }

    public String getManualCorrection() {
        return manualCorrection;
    }

    public void setManualCorrection(String manualCorrection) {
        this.manualCorrection = manualCorrection;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.JUNXING_ZLJ_300T;
    }

    @Override
    public String toString() {
        return "SensorJunXingZljInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", polynomialRatioA='" + polynomialRatioA + '\'' +
                ", temperatureCoefficientB='" + temperatureCoefficientB + '\'' +
                ", referenceValue='" + referenceValue + '\'' +
                ", CreateTemperature='" + CreateTemperature + '\'' +
                ", manualCorrection='" + manualCorrection + '\'' +
                '}';
    }
}
