package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：      生成ADME 电压配置参数拼接指令
 */
public class AdmeVoltageConfigEntity implements Validater {
    private String volt_power_over;//驱动器过压阈值
    private String volt_power_low;//驱动器低压阈值
    private String volt_power_under;//驱动器欠压阈值
    private String volt_sensor_low;//测斜仪低压阈值
    private String volt_sensor_under;//测斜仪欠压阈值

    public void setVolt_power_over(String volt_power_over) {
        this.volt_power_over = volt_power_over;
    }

    public void setVolt_power_low(String volt_power_low) {
        this.volt_power_low = volt_power_low;
    }

    public void setVolt_power_under(String volt_power_under) {
        this.volt_power_under = volt_power_under;
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
        if (volt_power_over != null && !volt_power_over.equals("NullKey")) {
            stringBuilder.append("volt_power_over=" + volt_power_over);
            stringBuilder.append("&");
        }
        if (volt_power_low != null && !volt_power_low.equals("NullKey")) {
            stringBuilder.append("volt_power_low=" + volt_power_low);
            stringBuilder.append("&");
        }
        if (volt_power_under != null && !volt_power_under.equals("NullKey")) {
            stringBuilder.append("volt_power_under=" + volt_power_under);
            stringBuilder.append("&");
        }
        if (volt_sensor_low != null && !volt_sensor_low.equals("NullKey")) {
            stringBuilder.append("volt_sensor_low=" + volt_sensor_low);
            stringBuilder.append("&");
        }
        if (volt_sensor_under != null && !volt_sensor_under.equals("NullKey")) {
            stringBuilder.append("volt_sensor_under=" + volt_sensor_under);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
