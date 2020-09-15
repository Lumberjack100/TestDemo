package com.shmedo.configlibrary.ble.enums;

/**
 * Created by adu on 2017/12/18.
 * DAS低功耗模式
 */
public enum LowEnergyModel {
    //待机
    STANDBY(1),
    //激活
    ACTIVATE(2);

    private int model;

    LowEnergyModel(int model) {
        this.model = model;
    }


    public int toInt() {
        return model;
    }

    public static LowEnergyModel valueOf(int model) {
        switch (model) {
            case 1:
                return STANDBY;

            case 2:
                return ACTIVATE;

            default:
                return STANDBY;
        }
    }
}
