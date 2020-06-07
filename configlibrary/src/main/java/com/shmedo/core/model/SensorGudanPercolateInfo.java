package com.shmedo.core.model;


import com.shmedo.core.interfaces.SensorParameter;
import com.shmedo.core.enums.SensorType;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型为葛南渗压计 VWP-03
 */
public class SensorGudanPercolateInfo implements SensorParameter {
    private int triggerThreshold;   //触发阈值
    private int sensitivityK;   //灵敏度K
    private double temperatureCoefficientB;//温度系数b
    private double datumValueF0; //基准值
    private double CreateTemperature;   //初始化温度T0
    private String manualCorrection;    //手动纠偏
    private double cordLenght;  //绳长
    private double installElevation;  //安装高程

    public int getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(int triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public int getSensitivityK() {
        return sensitivityK;
    }

    public void setSensitivityK(int sensitivityK) {
        this.sensitivityK = sensitivityK;
    }

    public double getTemperatureCoefficientB() {
        return temperatureCoefficientB;
    }

    public void setTemperatureCoefficientB(double temperatureCoefficientB) {
        this.temperatureCoefficientB = temperatureCoefficientB;
    }

    public double getDatumValueF0() {
        return datumValueF0;
    }

    public void setDatumValueF0(double datumValueF0) {
        this.datumValueF0 = datumValueF0;
    }

    public double getCreateTemperature() {
        return CreateTemperature;
    }

    public void setCreateTemperature(double createTemperature) {
        CreateTemperature = createTemperature;
    }

    public String getManualCorrection() {
        return manualCorrection;
    }

    public void setManualCorrection(String manualCorrection) {
        this.manualCorrection = manualCorrection;
    }

    public double getCordLenght() {
        return cordLenght;
    }

    public void setCordLenght(double cordLenght) {
        this.cordLenght = cordLenght;
    }

    public double getInstallElevation() {
        return installElevation;
    }

    public void setInstallElevation(double installElevation) {
        this.installElevation = installElevation;
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.GUDAN_PERCOLATE;
    }

    @Override
    public String toString() {
        return "SensorGudanPercolateInfo{" +
                "triggerThreshold=" + triggerThreshold +
                ", sensitivityK=" + sensitivityK +
                ", temperatureCoefficientB=" + temperatureCoefficientB +
                ", datumValueF0=" + datumValueF0 +
                ", CreateTemperature=" + CreateTemperature +
                ", manualCorrection='" + manualCorrection + '\'' +
                ", cordLenght=" + cordLenght +
                ", installElevation=" + installElevation +
                '}';
    }
}
