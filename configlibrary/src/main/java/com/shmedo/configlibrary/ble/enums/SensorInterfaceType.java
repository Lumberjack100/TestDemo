package com.shmedo.configlibrary.ble.enums;

/**
 * Created by adu on 2017/12/11.
 * 传感器接口类型
 */
public enum SensorInterfaceType {

    RS485(1),SDI12(2);

    private int type;
    SensorInterfaceType(int i) {
        this.type = i;
    }
    public int toInt(){
        return type;
    }

    public static SensorInterfaceType value(int type) {
        switch (type) {
            case 1: return RS485;
            case 2: return SDI12;
            default: return RS485;
        }
    }
}
