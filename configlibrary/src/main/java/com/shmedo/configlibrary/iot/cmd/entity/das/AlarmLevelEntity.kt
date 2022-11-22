package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：      生成报警级别参数拼接指令
 */
public class AlarmLevelEntity implements Validater {
    private String type;//传感器类型  1:雨量计,2:倾角计,3:主传感器
    private String level1;//无报警
    private String level2;//蓝色一级
    private String level3;//黄色二级
    private String level4;//橙色三级
    private String level5;//红色四级

    public void setType(String type) {
        this.type = type;
    }

    public void setLevel1(String level1) {
        this.level1 = level1;
    }

    public void setLevel2(String level2) {
        this.level2 = level2;
    }

    public void setLevel3(String level3) {
        this.level3 = level3;
    }

    public void setLevel4(String level4) {
        this.level4 = level4;
    }

    public void setLevel5(String level5) {
        this.level5 = level5;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("type");
            stringBuilder.append("=");
            stringBuilder.append(type);
            stringBuilder.append("&");
            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !f.getName().equals("type") && !value.equals("NullKey")) {
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
