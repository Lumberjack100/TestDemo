package com.shmedo.core.model;

/**
 * Created by adu on 2018/1/8.
 * 设置数字渗压计深度、温度修正值
 */
public class SetOsmometerCorrectInfo {
    private int depthCorrect; //深度修正
    private int temperatureCorrect; //温度修正

    public int getDepthCorrect() {
        return depthCorrect;
    }

    public void setDepthCorrect(int depthCorrect) {
        this.depthCorrect = depthCorrect;
    }

    public int getTemperatureCorrect() {
        return temperatureCorrect;
    }

    public void setTemperatureCorrect(int temperatureCorrect) {
        this.temperatureCorrect = temperatureCorrect;
    }

    @Override
    public String toString() {
        return "SetOsmometerCorrectInfo{" +
                "depthCorrect=" + depthCorrect +
                ", temperatureCorrect=" + temperatureCorrect +
                '}';
    }
}
