package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/27 <br/>
 * 描述：   手动/自动注册平台参数
 */
public class RegistrationPlatformEntity implements Validater {
    private int number;//服务器(数据中心)编号，取值1,2,3
    private String sNOrProductId;//设备SN号/产品ID
    private String productIdOrDeviceId;//产品ID/设备ID
    private String registrationCodeOrPwd;//注册码/设备KEY

    public RegistrationPlatformEntity(int number, String sNOrProductId, String productIdOrDeviceId, String registrationCodeOrPwd) {
        this.number = number;
        this.sNOrProductId = sNOrProductId;
        this.productIdOrDeviceId = productIdOrDeviceId;
        this.registrationCodeOrPwd = registrationCodeOrPwd;
    }

    @Override
    public void validate() {
        if (number != 1 && number != 2 && number != 3)
            throw new DASParameterException("服务器编号错误");

        if (TextUtils.isEmpty(sNOrProductId))
            throw new DASParameterException("设备SN号/产品ID不能为空");

        if (TextUtils.isEmpty(productIdOrDeviceId))
            throw new DASParameterException("产品ID/设备ID不能为空");

        if (TextUtils.isEmpty(registrationCodeOrPwd))
            throw new DASParameterException("注册码/设备KEY不能为空");
    }

    @Override
    public String toString() {
        return this.number + "" + this.sNOrProductId+ "," + this.productIdOrDeviceId+ "," + this.registrationCodeOrPwd;
    }
}
