package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/6/21 <br/>
 * 描述：     生成ADME测量孔深配置参数拼接指令
 */
public class AdmeMeasuringHoleDepthEntity implements Validater {
    private String movementway;//运动方式（0:上拉，1:下放）
    private String motorspeed;//电机速度
    private String movedistance;//运动距离

    public void setMovementway(String movementway) {
        this.movementway = movementway;
    }

    public void setMotorspeed(String motorspeed) {
        this.motorspeed = motorspeed;
    }

    public void setMovedistance(String movedistance) {
        this.movedistance = movedistance;
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
