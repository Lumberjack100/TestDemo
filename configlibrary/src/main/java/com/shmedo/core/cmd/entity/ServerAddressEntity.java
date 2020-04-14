package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/14.
 * 获取服务器地址参数
 */
public class ServerAddressEntity implements Validater{
    private int port;

    public ServerAddressEntity(int port) {
        this.port = port;
    }

    @Override
    public void validate() {
        if (port != 1 && port != 2 && port != 3)
            throw new DASParameterException("服务器地址不存在");
    }

    @Override
    public String toString() {
        return String.valueOf(this.port);
    }
}
