package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：      生成ADME 电压配置参数拼接指令
 */
public class AdmeVoltageConfigEntity implements Validater {
    private String driveovervolt;//驱动器过压阈值
    private String drivelowvolt;//驱动器低压阈值
    private String driveundervolt;//驱动器欠压阈值
    private String inclowvolt;//测斜仪低压阈值
    private String incundervolt;//测斜仪欠压阈值

    public void setDriveovervolt(String driveovervolt) {
        this.driveovervolt = driveovervolt;
    }

    public void setDrivelowvolt(String drivelowvolt) {
        this.drivelowvolt = drivelowvolt;
    }

    public void setDriveundervolt(String driveundervolt) {
        this.driveundervolt = driveundervolt;
    }

    public void setInclowvolt(String inclowvolt) {
        this.inclowvolt = inclowvolt;
    }

    public void setIncundervolt(String incundervolt) {
        this.incundervolt = incundervolt;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (driveovervolt != null && !driveovervolt.equals("NullKey")) {
            stringBuilder.append("driveovervolt=" + driveovervolt);
            stringBuilder.append("&");
        }
        if (drivelowvolt != null && !drivelowvolt.equals("NullKey")) {
            stringBuilder.append("drivelowvolt=" + drivelowvolt);
            stringBuilder.append("&");
        }
        if (driveundervolt != null && !driveundervolt.equals("NullKey")) {
            stringBuilder.append("driveundervolt=" + driveundervolt);
            stringBuilder.append("&");
        }
        if (inclowvolt != null && !inclowvolt.equals("NullKey")) {
            stringBuilder.append("inclowvolt=" + inclowvolt);
            stringBuilder.append("&");
        }
        if (incundervolt != null && !incundervolt.equals("NullKey")) {
            stringBuilder.append("incundervolt=" + incundervolt);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
