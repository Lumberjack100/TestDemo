package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/15.
 * 重启设备的参数
 */
public class RebootDeviceEntity implements Validater{

    private int time;

    public RebootDeviceEntity(int time) {
        this.time = time;
    }

    @Override
    public void validate() {
        if (time < 0 && time > 9999)
            throw new DASParameterException("时间设置错误");
    }

    @Override
    public String toString() {
        return String.valueOf(time);
    }
}
