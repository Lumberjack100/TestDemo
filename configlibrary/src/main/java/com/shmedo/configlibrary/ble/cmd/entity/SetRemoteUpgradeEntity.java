package com.shmedo.configlibrary.ble.cmd.entity;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/18.
 * 设置远程升级参数
 */
public class SetRemoteUpgradeEntity implements Validater {
    /**
     * x=0：关闭升级模式，Y…Y选填
     * x=1：打开升级模式，Y…Y为升级服务器端口号（最长支持5位数字）
     * x=2： 打开升级模式，Y…Y为服务器地址和端口，用空格分割
     */
    private int model;
    private String address;//服务器地址（可以为IP或域名）
    private int port;//端口号


    public SetRemoteUpgradeEntity(int model, String address, int port) {
        this.model = model;
        this.address = address;
        this.port = port;
    }

    @Override
    public void validate() {
        if (model != 0 && model != 1 && model != 2)
            throw new DASParameterException("升级模式错误");

        if (port < 0 || port > 65535)
            throw new DASParameterException("服务器端口号错误");

        if (model == 2 && TextUtils.isEmpty(address))
            throw new DASParameterException("服务器地址不能为空");
    }

    @Override
    public String toString() {
        switch (model) {
            case 1:
                return this.model + "" + this.port;

            case 2:
                return this.model + " " + this.address + " " + this.port;

            default:
                return this.model + "";
        }
    }
}
