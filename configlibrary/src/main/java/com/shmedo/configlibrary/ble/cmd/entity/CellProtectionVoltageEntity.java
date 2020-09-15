package com.shmedo.configlibrary.ble.cmd.entity;


import android.text.TextUtils;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/18.
 * 电池过放保护电压参数
 */
public class CellProtectionVoltageEntity implements Validater {
    private String voltage;

    public CellProtectionVoltageEntity(String voltage) {
        this.voltage = voltage;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(voltage))
            throw new DASParameterException("参数错误");
    }

    @Override
    public String toString() {
        return this.voltage;
    }
}
