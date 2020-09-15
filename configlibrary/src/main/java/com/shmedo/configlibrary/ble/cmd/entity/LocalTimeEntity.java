package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;

/**
 * Created by adu on 2017/12/15.
 * 本地时间参数
 */
public class LocalTimeEntity implements Validater {
    private String time;

    public LocalTimeEntity(String time) {
        this.time = time;
    }

    @Override
    public void validate() {
        //150526102201
        if (!ValidateUtil.cheakLocalTime(time))
            throw new DASParameterException("时间格式不正确");
    }


    @Override
    public String toString() {
        return this.time;
    }
}
