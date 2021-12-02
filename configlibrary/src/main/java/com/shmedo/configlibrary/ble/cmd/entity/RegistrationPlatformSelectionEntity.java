package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/27 <br/>
 * 描述：    注册平台选择参数
 */
public class RegistrationPlatformSelectionEntity implements Validater {
    private int number;//服务器(数据中心)编号

    private int registrationPlatform;//注册平台类型，0：地大平台，1：成都理工平台，2：米度平台

    public RegistrationPlatformSelectionEntity(int number, int registrationPlatform) {
        this.number = number;
        this.registrationPlatform = registrationPlatform;
    }

    @Override
    public void validate() {
    }

    @Override
    public String toString() {
        return this.number + "" + this.registrationPlatform;
    }
}
