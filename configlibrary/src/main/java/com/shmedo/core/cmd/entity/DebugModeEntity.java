package com.shmedo.core.cmd.entity;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.cmd.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：    设备调试模式参数
 */
public class DebugModeEntity implements Validater {
    private int model;

    public DebugModeEntity(int model) {
        this.model = model;
    }

    @Override
    public void validate() {
        if (model != 0 && model != 1 && model != 2 && model != 3)
            throw new DASParameterException("调试模式错误");
    }

    @Override
    public String toString() {
        return String.valueOf(this.model);
    }
}
