package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2018/1/8.
 * 设置数字水位计深度、温度触发值
 */
public class SetOsmometerTriggerInfo {
    private int depthTrigger; //深度触发
    private int temperatureTrigger; //温度触发

    public int getDepthTrigger() {
        return depthTrigger;
    }

    public void setDepthTrigger(int depthTrigger) {
        this.depthTrigger = depthTrigger;
    }

    public int getTemperatureTrigger() {
        return temperatureTrigger;
    }

    public void setTemperatureTrigger(int temperatureTrigger) {
        this.temperatureTrigger = temperatureTrigger;
    }

    @Override
    public String toString() {
        return "SetOsmometerTriggerInfo{" +
                "depthTrigger=" + depthTrigger +
                ", temperatureTrigger=" + temperatureTrigger +
                '}';
    }
}
