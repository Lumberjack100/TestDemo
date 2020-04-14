package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

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
