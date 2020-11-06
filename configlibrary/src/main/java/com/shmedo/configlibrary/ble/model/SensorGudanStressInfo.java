package com.shmedo.configlibrary.ble.model;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.SensorParameter;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型为葛南应力计 53
 */
public class SensorGudanStressInfo implements SensorParameter{
    private String triggerThreshold;   //触发阈值
    private String sensitivityK;   //灵敏度K
    private String temperatureCoefficientB;//温度系数b
    private String expansionCoefficient;    //膨胀系数
    private String datumValueF0; //基准值
    private String CreateTemperature;   //初始化温度T0
    private String manualCorrection;    //修正值

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

    public String getExpansionCoefficient() {
        return expansionCoefficient;
    }

    public void setExpansionCoefficient(String expansionCoefficient) {
        this.expansionCoefficient = expansionCoefficient;
    }

    public String getDatumValueF0() {
        return datumValueF0;
    }

    public void setDatumValueF0(String datumValueF0) {
        this.datumValueF0 = datumValueF0;
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
