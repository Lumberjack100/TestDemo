package com.shmedo.configlibrary.iot.cmd.entity.e40;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/6/18 <br/>
 * 描述：       生成GPS 工作参数拼接指令
 */
public class E40GpsWorkInfoEntity implements Validater {
    private String cutoffangle;//卫星仰角截止角 范围0-90度
    private String range;//观测范围  0或1
    private String savefreq;//数据频率

    public void setCutoffangle(String cutoffangle) {
        this.cutoffangle = cutoffangle;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setSavefreq(String savefreq) {
        this.savefreq = savefreq;
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
