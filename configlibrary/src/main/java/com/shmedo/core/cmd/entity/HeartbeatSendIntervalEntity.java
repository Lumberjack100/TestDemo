package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

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
    if (Integer.valueOf(time) < 0 && Integer.valueOf(time) >86400)
            throw new DASParameterException("参数错误");
    }

    @Override
    public String toString() {
        return this.time;
    }
}
