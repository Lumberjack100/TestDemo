package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/14.
 * 获取服务器(数据中心)编号参数
 */
public class ServerNumberEntity implements Validater{
    private int number;

    public ServerNumberEntity(int number) {
        this.number = number;
    }

    @Override
    public void validate() {
        if (number != 1 && number != 2 && number != 3)
            throw new DASParameterException("服务器编号不存在");
    }

    @Override
    public String toString() {
        return String.valueOf(this.number);
    }
}
