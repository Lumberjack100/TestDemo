package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.cmd.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：    设备安装位置
 */
public class InstallLocationEntity implements Validater {

    private int model;//1:同步位置  2:查询位置

    public InstallLocationEntity(int model) {
        this.model = model;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return String.valueOf(model);
    }
}
