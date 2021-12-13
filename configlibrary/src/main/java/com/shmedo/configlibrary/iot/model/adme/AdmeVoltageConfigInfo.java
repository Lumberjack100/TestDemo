package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：      ADME 电压配置参数
 */
public class AdmeVoltageConfigInfo {
    private String volt_power_over;//驱动器过压阈值
    private String volt_power_low;//驱动器低压阈值
    private String volt_power_under;//驱动器欠压阈值
    private String volt_sensor_low;//测斜仪低压阈值
    private String volt_sensor_under;//测斜仪欠压阈值

    public String getVolt_power_over() {
        return TextUtils.isEmpty(volt_power_over) ? "" : volt_power_over;
    }

    public void setVolt_power_over(String volt_power_over) {
        this.volt_power_over = volt_power_over;
    }

    public String getVolt_power_low() {
        return TextUtils.isEmpty(volt_power_low) ? "" : volt_power_low;
    }

    public void setVolt_power_low(String volt_power_low) {
        this.volt_power_low = volt_power_low;
    }

    public String getVolt_power_under() {
        return TextUtils.isEmpty(volt_power_under) ? "" : volt_power_under;
    }

    public void setVolt_power_under(String volt_power_under) {
        this.volt_power_under = volt_power_under;
    }

    public String getVolt_sensor_low() {
        return TextUtils.isEmpty(volt_sensor_low) ? "" : volt_sensor_low;
    }

    public void setVolt_sensor_low(String volt_sensor_low) {
        this.volt_sensor_low = volt_sensor_low;
    }

    public String getVolt_sensor_under() {
        return TextUtils.isEmpty(volt_sensor_under) ? "" : volt_sensor_under;
    }

    public void setVolt_sensor_under(String volt_sensor_under) {
        this.volt_sensor_under = volt_sensor_under;
    }
}
