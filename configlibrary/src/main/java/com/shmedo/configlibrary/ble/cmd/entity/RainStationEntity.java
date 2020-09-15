package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/15.
 * 雨量站参数
 */
public class RainStationEntity implements Validater {
    private int state;

    public RainStationEntity(int state) {
        this.state = state;
    }

    @Override
    public void validate() {
        if (state != 1 && state != 2 && state != 3)
            throw new DASParameterException("雨量站(开关量)状态错误");
    }

    @Override
    public String toString() {
        return String.valueOf(state);
    }
}
