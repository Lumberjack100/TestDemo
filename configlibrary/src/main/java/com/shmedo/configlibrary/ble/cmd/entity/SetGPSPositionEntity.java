package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/18.
 * 设置GPS定位参数
 */
public class SetGPSPositionEntity implements Validater {
    private String sensitivity;
    private int accuracy;


    public SetGPSPositionEntity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public SetGPSPositionEntity(String sensitivity, int accuracy) {
        this.sensitivity = sensitivity;
        this.accuracy = accuracy;
    }

    @Override
    public void validate() {
        if (accuracy == 0 || accuracy > 100000)
            throw new DASParameterException("定位精度参数错误");
    }

    @Override
    public String toString() {
        return this.accuracy != 0 ? this.sensitivity + "" + this.accuracy : this.sensitivity;
    }
}
