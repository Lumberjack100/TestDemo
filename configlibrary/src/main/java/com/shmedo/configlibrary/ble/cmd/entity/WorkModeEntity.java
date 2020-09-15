package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.cmd.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：    设备工作模式参数
 */
public class WorkModeEntity implements Validater {
    private int model;

    public WorkModeEntity(int model) {
        this.model = model;
    }

    @Override
    public void validate() {
        if (model != 0 && model != 1 && model != 2 && model != 3)
            throw new DASParameterException("工作模式错误");
    }

    @Override
    public String toString() {
        return String.valueOf(this.model);
    }
}
