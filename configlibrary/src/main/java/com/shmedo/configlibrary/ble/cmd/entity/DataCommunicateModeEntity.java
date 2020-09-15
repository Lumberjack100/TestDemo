package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/15.
 * 数据通讯模式参数
 */
public class DataCommunicateModeEntity implements Validater {

    private int model;

    public DataCommunicateModeEntity(int model) {
        this.model = model;
    }

    @Override
    public void validate() {
        if (model != 1 && model != 2 && model != 3 && model != 4)
            throw new DASParameterException("数据通讯模式有误");
    }

    @Override
    public String toString() {
        return String.valueOf(model);
    }
}
