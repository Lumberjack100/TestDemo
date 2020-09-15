package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/19.
 * 设置授权手机号码的实体类
 */
public class SetAuthorzePhoneInfo {
    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "SetAuthorzePhoneInfo{" +
                "phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
