package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/11.
 * 设备状态
 */
public enum EquipmentStatus {

    STANDBY(1),ACTIVATION(2);
    //1、待机  2、激活
    private int status;
    EquipmentStatus(int i) {
        this.status = i;
    }

    public int toInt() {
        return status;
    }

    public static EquipmentStatus valueOf(int status) {
        switch (status) {
            case 1:return STANDBY;
            case 2:return ACTIVATION;
            default: return STANDBY;
        }
    }
}
