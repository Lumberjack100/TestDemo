package com.shmedo.configlibrary.ble.cmd.entity;


import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2018/1/10.
 * 配置认证方式的参数
 */
public class AuthenticationConfigEntity implements Validater {
    private String sn;
    private int mode;//认证方式 0:普通认证 1:系统认证

    public AuthenticationConfigEntity(String sn, int mode) {
        this.sn = sn;
        this.mode = mode;
    }

    @Override
    public void validate() {
        //验证sn必须为7位字符
        if (sn.length() != 7)
            throw new DASParameterException("SN号有误");

        if (mode != 0 && mode != 1)
            throw new DASParameterException("认证方式错误");
    }

    @Override
    public String toString() {
        return "," + this.sn + "," + this.mode;
    }

}
