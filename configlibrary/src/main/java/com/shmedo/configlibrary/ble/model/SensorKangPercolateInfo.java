package com.shmedo.configlibrary.ble.model;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器为基康渗压计 BGK-4500
 */
public class SensorKangPercolateInfo implements SensorParameter {

    private String triggerThreshold;   //触发阈值
    private String polynomialRatioA;//多项式系数A
    private String polynomialRatioB;//多项式系数B
    private String polynomialRatioC;//多项式系数C
    private String temperatureCoefficientK;//温度系数K
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

    public String getPolynomialRatioA() {
        return polynomialRatioA;
    }

    public void setPolynomialRatioA(String polynomialRatioA) {
        this.polynomialRatioA = polynomialRatioA;
    }

    public String getPolynomialRatioB() {
        return polynomialRatioB;
    }

    public void setPolynomialRatioB(String polynomialRatioB) {
        this.polynomialRatioB = polynomialRatioB;
    }

    public String getPolynomialRatioC() {
        return polynomialRatioC;
    }

    public void setPolynomialRatioC(String polynomialRatioC) {
        this.polynomialRatioC = polynomialRatioC;
    }

    public String getTemperatureCoefficientK() {
        return temperatureCoefficientK;
    }

    public void setTemperatureCoefficientK(String temperatureCoefficientK) {
        this.temperatureCoefficientK = temperatureCoefficientK;
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
        return SensorType.KANG_PERCOLATE;
    }

    @Override
    public String toString() {
        return "SensorKangPercolateInfo{" +
                "triggerThreshold='" + triggerThreshold + '\'' +
                ", polynomialRatioA='" + polynomialRatioA + '\'' +
                ", polynomialRatioB='" + polynomialRatioB + '\'' +
                ", polynomialRatioC='" + polynomialRatioC + '\'' +
                ", temperatureCoefficientK='" + temperatureCoefficientK + '\'' +
                ", CreateTemperature='" + CreateTemperature + '\'' +
                ", manualCorrection='" + manualCorrection + '\'' +
                ", cordLenght='" + cordLenght + '\'' +
                ", installElevation='" + installElevation + '\'' +
                '}';
    }
}
