package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/11.
 * 雨量站
 */
public enum RainfallStation {
    RAIN_OPEN(1),RAIN_CLOSE(2),ALARM_OPEN(3);
    //1、开启      2、关闭
    private int status;
    RainfallStation(int i) {
        this.status = i;
    }
    public int toInt() {
        return status;
    }

    public static RainfallStation valueOf(int status) {
        switch (status) {
            case 1: return RAIN_OPEN;
            case 2: return RAIN_CLOSE;
            case 3: return ALARM_OPEN;
            default: return ALARM_OPEN;
        }
    }
}
