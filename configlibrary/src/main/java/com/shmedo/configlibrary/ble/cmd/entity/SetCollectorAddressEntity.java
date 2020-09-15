package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 * 采集器地址参数
 */
public class SetCollectorAddressEntity implements Validater {

    private int address;

    public SetCollectorAddressEntity(int address) {
        this.address = address;
    }

    @Override
    public void validate() {
        if (address < 0 || address > 255)
            throw new DASParameterException("采集器地址错误");
    }

    @Override
    public String toString() {
        return String.valueOf(address);
    }
}
