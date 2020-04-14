package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/19.
 * 采集器地址参数
 */
public class SetCollectorAddressEntity implements Validater{

    private int address;

    public SetCollectorAddressEntity(int address) {
        this.address = address;
    }

    @Override
    public void validate() {
        if (address < 0 && address > 100000)
            throw new DASParameterException("采集器地址错误");
    }

    @Override
    public String toString() {
        return String.valueOf(address);
    }
}
