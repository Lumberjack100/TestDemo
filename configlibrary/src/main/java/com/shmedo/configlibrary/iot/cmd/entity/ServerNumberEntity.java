package com.shmedo.configlibrary.iot.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/16/20 <br/>
 * 描述：     获取服务器(数据中心)编号参数
 */
public class ServerNumberEntity implements Validater {
    private int number;

    public ServerNumberEntity(int number) {
        this.number = number;
    }

    @Override
    public void validate() {
        if (number != 1 && number != 2 && number != 3 && number != 4)
            throw new DASParameterException("服务器编号不存在");
    }

    @Override
    public String toString() {
        return "centerid=" + number;
    }
}
