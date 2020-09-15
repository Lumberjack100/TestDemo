package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/18.
 * 设置电池过放保护电压实体类
 */
public class CellProtectionVoltageInfo {
    private double voltage;

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    @Override
    public String toString() {
        return "CellProtectionVoltageInfo{" +
                "voltage=" + voltage +
                '}';
    }
}
