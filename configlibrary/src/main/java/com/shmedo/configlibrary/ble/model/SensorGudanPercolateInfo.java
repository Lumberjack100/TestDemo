package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型为葛南渗压计 VWP-03
 */
public class SensorGudanPercolateInfo implements SensorParameter {
    private String triggerThreshold;   //触发阈值
    private String sensitivityK;   //灵敏度K
    private String temperatureCoefficientB;//温度系数b
    private String referenceValue; //基准值
    private String CreateTemperature;   //初始化温度T0
    private String manualCorrection;    //手动纠偏
    private String cordLenght;  //绳长
    private String installElevation;  //安装高程

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
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

    public String getManualCorrection() {
        return manualCorrection;
    }

    public void setManualCorrection(String manualCorrection) {
        this.manualCorrection = manualCorrection;
    }

    public String getCordLenght() {
        return cordLenght;
    }

    public void setCordLenght(String cordLenght) {
        this.cordLenght = cordLenght;
    }

    public String getInstallElevation() {
        return installElevation;
    }

    public void setInstallElevation(String installElevation) {
        this.installElevation = installElevation;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.GUDAN_PERCOLATE;
    }

    @Override
    public String toString() {
        return "SensorGudanPercolateInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", sensitivityK='" + sensitivityK + '\'' +
                ", temperatureCoefficientB='" + temperatureCoefficientB + '\'' +
                ", datumValueF0='" + referenceValue + '\'' +
                ", CreateTemperature='" + CreateTemperature + '\'' +
                ", manualCorrection='" + manualCorrection + '\'' +
                ", cordLenght='" + cordLenght + '\'' +
                ", installElevation='" + installElevation + '\'' +
                '}';
    }
}
