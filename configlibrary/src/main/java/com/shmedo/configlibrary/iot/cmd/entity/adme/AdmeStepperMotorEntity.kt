package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：     生成ADME 步进电机配置参数拼接指令
 */
public class AdmeStepperMotorEntity implements Validater {
    private String posnegtest;//正反测（0:关闭，1:开启）
    private String absprsion;//绝对精度修正值
    private String movspeed;//步进电机运动速度
    private String movesm;//步进电机力矩

    public void setPosnegtest(String posnegtest) {
        this.posnegtest = posnegtest;
    }

    public void setAbsprsion(String absprsion) {
        this.absprsion = absprsion;
    }

    public void setMovspeed(String movspeed) {
        this.movspeed = movspeed;
    }

    public void setMovesm(String movesm) {
        this.movesm = movesm;
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
