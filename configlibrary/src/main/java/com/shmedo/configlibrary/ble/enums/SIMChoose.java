package com.shmedo.configlibrary.ble.enums;

/**
 * Created by adu on 2018/3/22.
 */
public enum SIMChoose {
    SIM1(1),SIM2(2);

    private int model;
    SIMChoose(int i) {
        this.model = i;
    }
    public int toInt() {
        return model;
    }

    public static SIMChoose valueOf(int model) {
        switch (model) {
            case 1: return SIM1;
            case 2: return SIM2;
            default: return SIM1;
        }
    }
}
