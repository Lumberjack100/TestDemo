package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/14.
 * 获取服务器(数据中心)编号参数
 */
public class ServerNumberEntity implements Validater {
    private int number;

    public ServerNumberEntity(int number) {
        this.number = number;
    }

    @Override
    public void validate() {
    }

    @Override
    public String toString() {
        return String.valueOf(this.number);
    }
}
