package com.shmedo.core.model;

import com.shmedo.core.enums.SensorType;
import com.shmedo.core.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型为葛南应力计 53
 */
public class SensorGudanStressInfo implements SensorParameter{
    private int triggerThreshold;   //触发阈值
    private int sensitivityK;   //灵敏度K
    private double temperatureCoefficientB;//温度系数b
    private double expansionCoefficient;    //膨胀系数
    private double datumValueF0; //基准值
    private double CreateTemperature;   //初始化温度T0
    private String manualCorrection;    //手动纠偏

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

    public double getExpansionCoefficient() {
        return expansionCoefficient;
    }

    public void setExpansionCoefficient(double expansionCoefficient) {
        this.expansionCoefficient = expansionCoefficient;
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

    @Override
    public SensorType getSensorType() {
        return SensorType.GUDAN_STRESS;
    }

    @Override
    public String toString() {
        return "SensorGudanStressInfo{" +
                "triggerThreshold=" + triggerThreshold +
                ", sensitivityK=" + sensitivityK +
                ", temperatureCoefficientB=" + temperatureCoefficientB +
                ", expansionCoefficient=" + expansionCoefficient +
                ", datumValueF0=" + datumValueF0 +
                ", CreateTemperature=" + CreateTemperature +
                ", manualCorrection='" + manualCorrection + '\'' +
                '}';
    }
}
