package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2018/1/8.
 * 数字式渗压计参数
 */
public class DigitalOsmometerFunctionEntity implements Validater {
    private int osmometerStatus;

    public DigitalOsmometerFunctionEntity(int osmometerStatus) {
        this.osmometerStatus = osmometerStatus;
    }

    @Override
    public void validate() {
        if (osmometerStatus == 0)
            throw new DASParameterException("参数不能为空");
        if (osmometerStatus != 1 && osmometerStatus != 2)
            throw new DASParameterException("渗压计功能设置失败");
    }

    @Override
    public String toString() {
        return String.valueOf(this.osmometerStatus);
    }
}
