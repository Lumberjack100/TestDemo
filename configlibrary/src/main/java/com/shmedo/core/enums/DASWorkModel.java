package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/18.
 * DAS工作模式
 */
public enum DASWorkModel {
    //待机
    STANDBY(1),
    //激活
    ACTIVATE(2);

    private int model;
    DASWorkModel(int model) {
        this.model = model;
    }

    public static DASWorkModel valueOf(int model) {
        switch (model) {
            case 1: return STANDBY;
            case 2: return ACTIVATE;
            default:return STANDBY;
        }
    }
}
