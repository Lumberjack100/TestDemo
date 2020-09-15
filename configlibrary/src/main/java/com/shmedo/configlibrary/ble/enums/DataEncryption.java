package com.shmedo.configlibrary.ble.enums;

/**
 * Created by adu on 2018/3/22.
 * 数据加密
 */
public enum DataEncryption {

    CLEAR(1),CIPHER(2);

    private int model;
    DataEncryption(int i) {
        this.model = i;
    }
    public int toInt() {
        return model;
    }

    public static DataEncryption valueOf(int model) {
        switch (model) {
            case 1: return CLEAR;
            case 2: return CIPHER;
            default: return CLEAR;
        }
    }
}
