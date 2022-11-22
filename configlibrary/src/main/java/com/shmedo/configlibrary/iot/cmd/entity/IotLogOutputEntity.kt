package com.shmedo.configlibrary.iot.cmd.entity;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：   生成设备日志输出物联网拼接指令
 */
public class IotLogOutputEntity implements Validater {
    private String level;//日志输出等级包含off、debug、info
    private String type;//输出方式包括uart、bt、net、file

    public void setLevel(String level) {
        this.level = level;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("level=" + level);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(type)) {
            stringBuilder.append("type=" + type);
            stringBuilder.append("&");
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
