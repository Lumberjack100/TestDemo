package com.shmedo.core.cmd.entity;


import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.utils.ValidateUtil;

/**
 * Created by adu on 2017/12/15.
 * 授权手机号码参数
 */
public class AuthorPhoneNumberEntity implements Validater {
    private String phoneNUmber;

    public AuthorPhoneNumberEntity(String phoneNUmber) {
        this.phoneNUmber = phoneNUmber;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(phoneNUmber))
            throw new DASParameterException("手机号不能为空");
        if (!ValidateUtil.checkMobileNumber(this.phoneNUmber))
            throw new DASParameterException("手机号码有误，请重试！");

    }

    @Override
    public String toString() {
        return this.phoneNUmber;
    }
}
