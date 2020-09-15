package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2018/1/11.
 * DAS发送认证结果
 */
public class DASAuthenticResultEntity implements Validater {

    private int result;

    public DASAuthenticResultEntity(int result) {
        this.result = result;
    }

    @Override
    public void validate() {
        if (result != 0 && result != 1)
            throw new DASParameterException("认证结果参数异常");
    }

    @Override
    public String toString() {
        return String.valueOf(this.result);
    }
}
