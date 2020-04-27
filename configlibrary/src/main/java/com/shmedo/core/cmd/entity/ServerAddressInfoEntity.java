package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 * 设置服务器(数据中心)地址端口参数
 */
public class ServerAddressInfoEntity implements Validater {
    private int number;//服务器(数据中心)编号，取值1,2,3
    private String address;//服务器地址（可以为IP或域名）
    private int port;//服务器端口（最大65535）

    public ServerAddressInfoEntity(int number, String address, int port) {
        this.number = number;
        this.address = address;
        this.port = port;
    }

    @Override
    public void validate() {
        if (number != 1 && number != 2 && number != 3)
            throw new DASParameterException("服务器编号错误");

        if (TextUtils.isEmpty(address))
            throw new DASParameterException("服务器地址不能为空");

        if (port < 0 || port > 65535)
            throw new DASParameterException("服务器端口号错误");
    }

    @Override
    public String toString() {
        return this.number + " " + this.address + " " + this.port;
    }
}
