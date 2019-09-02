package com.shmedo.mcloudapp.entity.ble.collector;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble.collector
 * 文件名:   SensorGudanPercolateInfoSub
 * 创建者:   dpc
 * 创建时间:  2019/5/9 14:40
 * 描述：   传感器类型为葛南渗压计 51
 */
public class SensorGudanPercolateInfoSub {
    private int triggerThreshold;   //触发阈值
    private int sensitivityK;   //灵敏度K
    private double temperatureCoefficientB;//温度系数b
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
    public String toString() {
        return "SensorGudanPercolateInfo{" +
            "triggerThreshold=" + triggerThreshold +
            ", sensitivityK=" + sensitivityK +
            ", temperatureCoefficientB=" + temperatureCoefficientB +
            ", datumValueF0=" + datumValueF0 +
            ", CreateTemperature=" + CreateTemperature +
            ", manualCorrection='" + manualCorrection + '\'' +
            '}';
    }
}
