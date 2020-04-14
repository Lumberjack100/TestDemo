package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/19.
 *  设置雨量计精度参数
 */
public class SettingRainPrecisionEntity implements Validater{
    private int precision;

    public SettingRainPrecisionEntity(int precision) {
        this.precision = precision;
    }

    @Override
    public void validate() {
        if (precision <= 0 && precision > 100000)
            throw new DASParameterException("雨量计精度参数错误");
    }

    @Override
    public String toString() {
        return String.valueOf(precision);
    }
}
