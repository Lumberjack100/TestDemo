package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

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
