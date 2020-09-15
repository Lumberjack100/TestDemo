package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/18.
 * DAS工作模式参数
 */
public class LowEnergyModelEntity implements Validater {
    private int model;

    public LowEnergyModelEntity(int model) {
        this.model = model;
    }

    @Override
    public void validate() {
        if (model != 1 && model != 2)
            throw new DASParameterException("DAS工作模式错误");
    }

    @Override
    public String toString() {
        return String .valueOf(this.model);
    }
}
