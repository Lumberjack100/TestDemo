package com.shmedo.configlibrary.iot.cmd.entity.hac;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/22 <br/>
 * 描述：     TODO
 */
public class HacMeasuringHoleDepthInfoEntity implements Validater {
    private String address;//MAC 地址
    private String holeno;//孔号
    private String areano;//区号
    private String lowtbtss;//下放堵转检测（0:关闭，1:开启）
    private String motorspeed;//电机速度
    private String measway;//测量模式（0:自动测量，1:手动测量）
    private String safedistance;//管底补偿距离
    private String movementway;//运动方式（0:上拉，1:下放）
    private String movedistance;//设定运动距离

    public void setAddress(String address) {
        this.address = address;
    }

    public void setHoleno(String holeno) {
        this.holeno = holeno;
    }

    public void setAreano(String areano) {
        this.areano = areano;
    }

    public void setLowtbtss(String lowtbtss) {
        this.lowtbtss = lowtbtss;
    }

    public void setMotorspeed(String motorspeed) {
        this.motorspeed = motorspeed;
    }

    public void setMeasway(String measway) {
        this.measway = measway;
    }

    public void setSafedistance(String safedistance) {
        this.safedistance = safedistance;
    }

    public void setMovementway(String movementway) {
        this.movementway = movementway;
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
