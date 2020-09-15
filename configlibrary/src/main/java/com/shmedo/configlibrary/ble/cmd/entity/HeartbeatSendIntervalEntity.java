package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/18.
 * 心跳包间隔参数
 */
public class HeartbeatSendIntervalEntity implements Validater {
    private String time;

    public HeartbeatSendIntervalEntity(String time) {
        this.time = time;
    }

    @Override
    public void validate() {
        //验证间隔格式
    if (Integer.parseInt(time) < 0 || Integer.parseInt(time) >86400)
            throw new DASParameterException("参数错误");
    }

    @Override
    public String toString() {
        return this.time;
    }
}
