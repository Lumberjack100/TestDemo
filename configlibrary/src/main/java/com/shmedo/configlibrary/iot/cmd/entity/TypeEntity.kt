package com.shmedo.configlibrary.iot.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：     TODO
 */
public class TypeEntity implements Validater {
    private String type;

    public TypeEntity(String type) {
        this.type = type;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return "type=" + type;
    }
}
