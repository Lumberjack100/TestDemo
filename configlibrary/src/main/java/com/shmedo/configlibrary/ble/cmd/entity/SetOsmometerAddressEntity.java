package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2018/1/8.
 * 设置数字渗压地址参数
 */
public class SetOsmometerAddressEntity implements Validater {
    private int address;

    public SetOsmometerAddressEntity(int address) {
        this.address = address;
    }

    @Override
    public void validate() {
        if (address < 0 || address > 255)
            throw new DASParameterException("设置数字式渗压地址错误");
    }

    @Override
    public String toString() {
        return String.valueOf(this.address);
    }
}
