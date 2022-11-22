package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/20 <br/>
 * 描述：      生成DAS 数据上报时间参数拼接指令
 */
public class DasDataReportEntity implements Validater {
    private String report_intv;//数据上报间隔
    private String plus_intv;//加报间隔
    private String plus_count;//加报次数

    public void setReport_intv(String report_intv) {
        this.report_intv = report_intv;
    }

    public void setPlus_intv(String plus_intv) {
        this.plus_intv = plus_intv;
    }

    public void setPlus_count(String plus_count) {
        this.plus_count = plus_count;
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
