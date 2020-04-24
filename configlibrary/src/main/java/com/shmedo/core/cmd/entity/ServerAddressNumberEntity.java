package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/14.
 * 获取服务器地址参数
 */
public class ServerAddressNumberEntity implements Validater{
    private int number;

    public ServerAddressNumberEntity(int number) {
        this.number = number;
    }

    @Override
    public void validate() {
        if (number != 1 && number != 2 && number != 3)
            throw new DASParameterException("服务器地址不存在");
    }

    @Override
    public String toString() {
        return String.valueOf(this.number);
    }
}
