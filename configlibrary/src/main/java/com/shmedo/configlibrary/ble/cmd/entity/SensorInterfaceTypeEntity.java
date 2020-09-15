package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/18.
 * 设置传感器接口类型参数
 */
public class SensorInterfaceTypeEntity implements Validater {

    private int type;

    public SensorInterfaceTypeEntity(int type) {
        this.type = type;
    }

    @Override
    public void validate() {
        if (type != 1 && type != 2)
            throw new DASParameterException("传感器接口类型错误");
    }

    @Override
    public String toString() {

        return String.valueOf(type);
    }
}
