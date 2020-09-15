package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 * 设置数据上报间隔参数
 */
public class DataReportIntervalEntity implements Validater {
    private int time;

    public DataReportIntervalEntity(int time) {
        this.time = time;
    }

    @Override
    public void validate() {
        if (time <= 0 || time > 100000)
            throw new DASParameterException("数据上报间隔参数错误");
    }

    @Override
    public String toString() {
        return String.valueOf(time);
    }
}
