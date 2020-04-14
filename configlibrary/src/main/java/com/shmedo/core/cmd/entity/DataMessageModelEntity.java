package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/15.
 * 数据通讯模式参数
 */
public class DataMessageModelEntity implements Validater {

    private int model;

    public DataMessageModelEntity(int model) {
        this.model = model;
    }

    @Override
    public void validate() {
        if (model != 1 && model != 2 && model != 3)
            throw new DASParameterException("数据通讯模式有误");
    }

    @Override
    public String toString() {
        return String.valueOf(model);
    }
}
