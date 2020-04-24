package com.shmedo.core.cmd.entity;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.cmd.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：   设置设备日志输出状态
 */
public class LogOutputEntity implements Validater {
    private int status;

    public LogOutputEntity(int status) {
        this.status = status;
    }

    @Override
    public void validate() {
        if (status != 0 && status != 1)
            throw new DASParameterException("日志输出状态错误");
    }

    @Override
    public String toString() {
        return String.valueOf(this.status);
    }
}