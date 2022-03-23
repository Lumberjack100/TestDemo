package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：      生成声光报警器参数拼接指令
 */
public class AudibleAlarmEntity implements Validater {
    private String channell;//通信信道
    private String panid;//网络编号
    private String groupid;//目标地址
    private String alarmtype;//报警类型

    public void setChannell(String channell) {
        this.channell = channell;
    }

    public void setPanid(String panid) {
        this.panid = panid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public void setAlarmtype(String alarmtype) {
        this.alarmtype = alarmtype;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("alarmtype");
            stringBuilder.append("=");
            stringBuilder.append(alarmtype);
            stringBuilder.append("&");
            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !f.getName().equals("alarmtype") && !value.equals("NullKey")) {
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
