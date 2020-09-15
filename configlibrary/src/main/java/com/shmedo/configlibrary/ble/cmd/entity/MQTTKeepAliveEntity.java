package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/27 <br/>
 * 描述： 设置 MQTT KeepAlive 参数
 */
public class MQTTKeepAliveEntity implements Validater {
    private int number;//服务器(数据中心)编号，取值1,2,3
    private int keepAlive;//服务器端口（最大65535）

    public MQTTKeepAliveEntity(int number, int keepAlive) {
        this.number = number;
        this.keepAlive = keepAlive;
    }

    @Override
    public void validate() {
        if (number != 1 && number != 2 && number != 3)
            throw new DASParameterException("服务器编号错误");

        if (keepAlive < 0 || keepAlive > 65535)
            throw new DASParameterException("keepAlive错误");
    }

    @Override
    public String toString() {
        return this.number + "" + this.keepAlive;
    }
}
