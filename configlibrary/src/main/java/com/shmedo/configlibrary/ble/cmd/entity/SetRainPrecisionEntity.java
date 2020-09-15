package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 *  设置雨量计精度参数
 */
public class SetRainPrecisionEntity implements Validater {
    private int precision;

    public SetRainPrecisionEntity(int precision) {
        this.precision = precision;
    }

    @Override
    public void validate() {
        if (precision <= 0 || precision > 100000)
            throw new DASParameterException("雨量计精度参数错误");
    }

    @Override
    public String toString() {
        return String.valueOf(precision);
    }
}
