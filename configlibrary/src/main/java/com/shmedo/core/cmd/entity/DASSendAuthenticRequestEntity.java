package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2018/1/11.
 * DAS发送认证请求
 */
public class DASSendAuthenticRequestEntity implements Validater {

    private String sn;

    public DASSendAuthenticRequestEntity(String sn) {
        this.sn = sn;
    }

    @Override
    public void validate() {
        if (sn.length() != 7)
            throw new DASParameterException("SN设备编号错误");
    }

    @Override
    public String toString() {
        return this.sn;
    }
}
