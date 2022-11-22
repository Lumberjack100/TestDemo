package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/24/20 <br/>
 * 描述：     ADME 设备模式
 */
public class AdmeEquipModelEntity implements Validater {
    private String model;

    public AdmeEquipModelEntity(String model) {
        this.model = model;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return "equimodel=" + model;
    }
}
