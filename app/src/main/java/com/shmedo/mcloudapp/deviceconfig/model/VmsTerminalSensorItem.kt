package com.shmedo.mcloudapp.deviceconfig.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/23/20 <br/>
 * 描述：     Vms 终端传感器
 */
public class VmsTerminalSensorItem {
    private String channel;//传感器通道
    private int resId;
    private boolean isInsert = false;//接入判断，0：未接入，1：接入

    public String getChannel() {
        return TextUtils.isEmpty(channel) ? "" : channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public void setInsert(boolean insert) {
        isInsert = insert;
    }
}
