package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.das.cmd.entity
 * 文件名:   SaveConfigInfoEntity
 * 创建者:   dpc
 * 创建时间:  2018/4/18 10:54
 * 描述：    保存配置信息参数
 */

public class SaveConfigInfoEntity  implements Validater {
    private int model;


    public SaveConfigInfoEntity(int model) {
        this.model = model;
    }


    @Override public void validate() {
        if (model != 1 && model != 2)
            throw new DASParameterException("保存配置信息模式错误");
    }


    @Override public String toString() {
        return String.valueOf(this.model);
    }
}
