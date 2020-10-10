package com.shmedo.configlibrary.ble.enums;

/**
 * Created by adu on 2018/1/8.
 * 渗压计功能状态
 */
public enum OsmometerStatus {

    OSMOMETER_OPEN(1), OSMOMETER_CLOSE(2);
    //1、开启      2、关闭
    private int status;

    OsmometerStatus(int i) {
        this.status = i;
    }

    public int toInt() {
        return status;
    }

    public static OsmometerStatus valueOf(int status) {
        switch (status) {
            case 1:
                return OSMOMETER_OPEN;
            case 2:
                return OSMOMETER_CLOSE;
            default:
                return OSMOMETER_CLOSE;
        }
    }
}
