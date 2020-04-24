package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/18.
 * 设置远程升级参数
 */
public class SetRemoteUpgradeEntity implements Validater {
    private int model;
    private int port;

    public SetRemoteUpgradeEntity(int model, int port) {
        this.model = model;
        this.port = port;
    }

    @Override
    public void validate() {
        if (model != 0 && model != 1)
            throw new DASParameterException("升级模式错误");
    }

    @Override
    public String toString() {
        return this.model + "" + this.port;
    }
}
