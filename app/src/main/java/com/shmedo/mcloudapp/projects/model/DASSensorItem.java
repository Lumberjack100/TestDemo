package com.shmedo.mcloudapp.projects.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/12 <br/>
 * 描述：     TODO #gh#
 */
public class DASSensorItem {
    private String sensorAddress;
    private int resId;
    private boolean isRemoveState = false;//是否处于可移除状态
    private boolean isAddButton = false;

    public DASSensorItem(int resId, boolean isAddButton) {
        this.resId = resId;
        this.isAddButton = isAddButton;
    }

    public DASSensorItem(int resId, boolean isAddButton, String sensorAddress) {
        this.resId = resId;
        this.isAddButton = isAddButton;
        this.sensorAddress = sensorAddress;
    }

    public String getSensorAddress() {
        return TextUtils.isEmpty(sensorAddress) ? "" : sensorAddress;
    }

    public void setSensorAddress(String sensorAddress) {
        this.sensorAddress = sensorAddress;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public boolean isRemoveState() {
        return isRemoveState;
    }

    public void setRemoveState(boolean removeState) {
        isRemoveState = removeState;
    }

    public boolean isAddButton() {
        return isAddButton;
    }

    public void setAddButton(boolean addButton) {
        isAddButton = addButton;
    }
}
