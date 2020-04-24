package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.cmd.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：    设备安装位置
 */
public class InstallLocationEntity implements Validater {

    private int model;

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
