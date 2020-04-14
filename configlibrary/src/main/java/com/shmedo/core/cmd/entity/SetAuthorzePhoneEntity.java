package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.utils.ValidateUtil;

/**
 * Created by adu on 2017/12/19.
 * 设置授权手机号码参数
 */
public class SetAuthorzePhoneEntity implements Validater {
    private String phoneNumber;

    public SetAuthorzePhoneEntity(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(phoneNumber))
            throw new DASParameterException("手机号码不能为空");
        if (!ValidateUtil.checkCellphone(this.phoneNumber))
            throw new DASParameterException("手机号码有误");
    }

    @Override
    public String toString() {
        return phoneNumber;
    }
}
