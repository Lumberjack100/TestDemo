package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 * 设置服务器地址端口参数
 */
public class SetServerAddressPortEntity implements Validater{
    private int number;
    private String address;
    private int port;

    public SetServerAddressPortEntity(int number, String address, int port) {
        this.number = number;
        this.address = address;
        this.port = port;
    }

    @Override
    public void validate() {
        if (number == 0 && TextUtils.isEmpty(address) && port == 0)
            throw new DASParameterException("不能为空");

        if (number != 1 && number != 2 && number != 3)
            throw new DASParameterException("服务器编号错误");
    }

    @Override
    public String toString() {
        return this.number+" "+this.address+" "+this.port;
    }
}
