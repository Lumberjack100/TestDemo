package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/3 <br/>
 * 描述：     传感器为应力计
 */
public class SensorStressGaugeInfo implements SensorParameter, Parcelable {
    private String triggerThreshold;   //触发阈值
    private String manualCorrection;    //修正值
    private String sensitivityK;//灵敏度 K
    private String temperatureCoefficientB;//温度系数b
    private String referenceValue; //基准值
    private String CreateTemperature;   //初始化温度T0
    private String elasticMode;// 弹性模量


    protected SensorStressGaugeInfo(Parcel in) {
        triggerThreshold = in.readString();
        manualCorrection = in.readString();
        sensitivityK = in.readString();
        temperatureCoefficientB = in.readString();
        referenceValue = in.readString();
        CreateTemperature = in.readString();
        elasticMode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerThreshold);
        dest.writeString(manualCorrection);
        dest.writeString(sensitivityK);
        dest.writeString(temperatureCoefficientB);
        dest.writeString(referenceValue);
        dest.writeString(CreateTemperature);
        dest.writeString(elasticMode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorStressGaugeInfo> CREATOR = new Creator<SensorStressGaugeInfo>() {
        @Override
        public SensorStressGaugeInfo createFromParcel(Parcel in) {
            return new SensorStressGaugeInfo(in);
        }

        @Override
        public SensorStressGaugeInfo[] newArray(int size) {
            return new SensorStressGaugeInfo[size];
        }
    };

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public String getManualCorrection() {
        return manualCorrection;
    }

    public void setManualCorrection(String manualCorrection) {
        this.manualCorrection = manualCorrection;
    }

    public String getSensitivityK() {
        return sensitivityK;
    }

    public void setSensitivityK(String sensitivityK) {
        this.sensitivityK = sensitivityK;
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

    public String getElasticMode() {
        return elasticMode;
    }

    public void setElasticMode(String elasticMode) {
        this.elasticMode = elasticMode;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.GUDAN_STRESS;

    }

    @Override
    public String toString() {
        return "SensorStressGaugeInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", manualCorrection='" + manualCorrection + '\'' +
                ", sensitivityK='" + sensitivityK + '\'' +
                ", temperatureCoefficientB='" + temperatureCoefficientB + '\'' +
                ", referenceValue='" + referenceValue + '\'' +
                ", CreateTemperature='" + CreateTemperature + '\'' +
                ", elasticMode='" + elasticMode + '\'' +
                '}';
    }
}
