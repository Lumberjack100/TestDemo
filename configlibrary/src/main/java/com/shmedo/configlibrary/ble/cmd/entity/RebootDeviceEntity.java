package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/15.
 * 重启设备的参数
 */
public class RebootDeviceEntity implements Validater {
    private int time;

    public RebootDeviceEntity(int time) {
        this.time = time;
    }

    @Override
    public void validate() {
        if (time < 0 || time > 9999)
            throw new DASParameterException("时间设置错误");
    }

    @Override
    public String toString() {
        return String.valueOf(time);
    }
}
