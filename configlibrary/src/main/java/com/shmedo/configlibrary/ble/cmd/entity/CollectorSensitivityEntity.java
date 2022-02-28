package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/28 <br/>
 * 描述：     设置 BHY 采集器灵敏度参数
 */
public class CollectorSensitivityEntity implements Validater {
    private String sensitivity;

    public CollectorSensitivityEntity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return this.sensitivity;
    }
}
