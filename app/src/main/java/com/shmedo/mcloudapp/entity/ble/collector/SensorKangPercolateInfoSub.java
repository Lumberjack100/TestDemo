package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   SensorKangPercolateInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:43
 * 描述：    传感器为基康渗压计  50
 */
public class SensorKangPercolateInfoSub {
    private int triggerThreshold;   //触发阈值
    private String polynomialRatioA;//多项式系数A
    private String polynomialRatioB;//多项式系数B
    private String polynomialRatioC;//多项式系数C
    private double temperatureCoefficientK;//温度系数K
    private double createTemperature;   //初始化温度T0
    private String manualCorrection;    //手动纠偏

    public int getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(int triggerThreshold) {
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

    public double getTemperatureCoefficientK() {
        return temperatureCoefficientK;
    }

    public void setTemperatureCoefficientK(double temperatureCoefficientK) {
        this.temperatureCoefficientK = temperatureCoefficientK;
    }

    public double getCreateTemperature() {
        return createTemperature;
    }

    public void setCreateTemperature(double createTemperature) {
        this.createTemperature = createTemperature;
    }

    public String getManualCorrection() {
        return manualCorrection;
    }

    public void setManualCorrection(String manualCorrection) {
        this.manualCorrection = manualCorrection;
    }

    @Override
    public String toString() {
        return "SensorKangPercolateInfo{" +
            "triggerThreshold=" + triggerThreshold +
            ", polynomialRatioA='" + polynomialRatioA + '\'' +
            ", polynomialRatioB='" + polynomialRatioB + '\'' +
            ", polynomialRatioC='" + polynomialRatioC + '\'' +
            ", temperatureCoefficientK=" + temperatureCoefficientK +
            ", createTemperature=" + createTemperature +
            ", manualCorrection='" + manualCorrection + '\'' +
            '}';
    }
}
