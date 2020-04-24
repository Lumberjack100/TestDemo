package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/18.
 * 设置传感器口波特率参数
 */
public class SensorBaudRateEntity implements Validater {
    private String baudRate;

    public SensorBaudRateEntity(String baudRate) {
        this.baudRate = baudRate;
    }

    @Override
    public void validate() {
        int baud = Integer.parseInt(baudRate);
        if (baud != 1200 && baud != 2400 && baud != 4800 && baud != 9600 && baud != 19200 && baud != 57600 && baud != 115200 )
            throw new DASParameterException("波特率参数错误");
    }

    @Override
    public String toString() {
        return this.baudRate;
    }
}
