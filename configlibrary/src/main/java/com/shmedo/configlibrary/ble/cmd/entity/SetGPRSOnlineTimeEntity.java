package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2018/1/4.
 * gprs持续时长参数
 */
public class SetGPRSOnlineTimeEntity implements Validater {
    private int time;

    public SetGPRSOnlineTimeEntity(int time) {
        this.time = time;
    }

    @Override
    public void validate() {
        if (time == 0)
            throw new DASParameterException("参数不能为空");

        if (time < 0 || time > 65536) {
            throw new DASParameterException("设置持续在线时长错误");
        }
    }

    @Override
    public String toString() {
        return String.valueOf(time);
    }
}
