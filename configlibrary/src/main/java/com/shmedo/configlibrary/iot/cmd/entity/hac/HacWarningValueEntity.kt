package com.shmedo.configlibrary.iot.cmd.entity.hac;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/20 <br/>
 * 描述：     预警值
 */
public class HacWarningValueEntity implements Validater {
    private String x1min;//一级预警 X 轴最小值
    private String x1max;//一级预警 X 轴最大值
    private String y1min;//一级预警 Y 轴最小值
    private String y1max;//一级预警 Y 轴最大值
    private String x2min;//二级预警 X 轴最小值
    private String x2max;//二级预警 X 轴最大值
    private String y2min;//二级预警 Y 轴最小值
    private String y2max;//二级预警 Y 轴最大值
    private String x3min;//三级预警 X 轴最小值
    private String x3max;//三级预警 X 轴最大值
    private String y3min;//三级预警 Y 轴最小值
    private String y3max;//三级预警 Y 轴最大值

    public void setX1min(String x1min) {
        this.x1min = x1min;
    }

    public void setX1max(String x1max) {
        this.x1max = x1max;
    }

    public void setY1min(String y1min) {
        this.y1min = y1min;
    }

    public void setY1max(String y1max) {
        this.y1max = y1max;
    }

    public void setX2min(String x2min) {
        this.x2min = x2min;
    }

    public void setX2max(String x2max) {
        this.x2max = x2max;
    }

    public void setY2min(String y2min) {
        this.y2min = y2min;
    }

    public void setY2max(String y2max) {
        this.y2max = y2max;
    }

    public void setX3min(String x3min) {
        this.x3min = x3min;
    }

    public void setX3max(String x3max) {
        this.x3max = x3max;
    }

    public void setY3min(String y3min) {
        this.y3min = y3min;
    }

    public void setY3max(String y3max) {
        this.y3max = y3max;
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
