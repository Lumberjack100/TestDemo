package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/27 <br/>
 * 描述：   数据中心链路、通讯协议参数
 */
public class DataCenterCommunicateProtoclEntity implements Validater {
    private int number;//服务器(数据中心)编号
    private int protocl;//通讯协议 2：MDM协议，4：MQTT自动注册，5：MQTT手动注册

    public DataCenterCommunicateProtoclEntity(int number, int protocl) {
        this.number = number;
        this.protocl = protocl;
    }

    @Override
    public void validate() {
    }

    @Override
    public String toString() {
        return this.number + "" + this.protocl;
    }


}
