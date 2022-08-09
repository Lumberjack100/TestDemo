package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：      生成ADME 电压配置参数拼接指令
 */
public class AdmeVoltageConfigEntity implements Validater {
    private String volt_power_standard;//驱动器标压阈值
    private String volt_power_low;//驱动器低压阈值
    private String volt_power_under;//驱动器欠压阈值
    private String volt_sensor_standard;//测斜仪标压阈值
    private String volt_sensor_low;//测斜仪低压阈值
    private String volt_sensor_under;//测斜仪欠压阈值

    public void setVolt_power_standard(String volt_power_standard) {
        this.volt_power_standard = volt_power_standard;
    }

    public void setVolt_power_low(String volt_power_low) {
        this.volt_power_low = volt_power_low;
    }

    public void setVolt_power_under(String volt_power_under) {
        this.volt_power_under = volt_power_under;
    }

    public void setVolt_sensor_standard(String volt_sensor_standard) {
        this.volt_sensor_standard = volt_sensor_standard;
    }

    public void setVolt_sensor_low(String volt_sensor_low) {
        this.volt_sensor_low = volt_sensor_low;
    }

    public void setVolt_sensor_under(String volt_sensor_under) {
        this.volt_sensor_under = volt_sensor_under;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !value.equals("NullKey")) {
                    stringBuilder.append(f.getName());
                    stringBuilder.append("=");
                    stringBuilder.append(value);
                    stringBuilder.append("&");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
