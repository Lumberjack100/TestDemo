package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2018/1/11.
 * DAS发送认证请求
 */
public class DASAuthenticRequestEntity implements Validater {

    private String sn;

    public DASAuthenticRequestEntity(String sn) {
        this.sn = sn;
    }

    @Override
    public void validate() {
        if (sn.length() != 7)
            throw new DASParameterException("设备SN错误");
    }

    @Override
    public String toString() {
        return this.sn;
    }
}
