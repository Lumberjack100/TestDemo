package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2018/1/11.
 * DAS发送认证结果
 */
public class DASSendAuthenticResultEntity implements Validater {

    private int result;

    public DASSendAuthenticResultEntity(int result) {
        this.result = result;
    }

    @Override
    public void validate() {
        if (result != 0 && result != 1)
            throw new DASParameterException("认证结果参数异常");
    }

    @Override
    public String toString() {
        return String.valueOf(this.result);
    }
}
