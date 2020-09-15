package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.SensorInterfaceType;

/**
 * Created by adu on 2017/12/18.
 * 设置传感器接口类型的实体类
 */
public class SensorInterfaceTypeInfo {
    private SensorInterfaceType type;

    public SensorInterfaceType getType() {
        return type;
    }

    public void setType(SensorInterfaceType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SensorInterfaceTypeInfo{" +
                "type=" + type +
                '}';
    }
}
