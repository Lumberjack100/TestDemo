package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/11.
 * 数据通讯模式
 */
public enum DataCommunicateMode {

    GPRS(1),SMS(2),BD(3),BD4G(4);

    private int mode;
    DataCommunicateMode (int i) {
        this.mode = i;
    }
    public int toInt(){
        return mode;
    }

    public static DataCommunicateMode valueOf(int mode) {
        switch (mode) {
            case 1: return GPRS;
            case 2: return SMS;
            case 3: return BD;
            case 4: return BD4G;
            default: return GPRS;
        }
    }
}
