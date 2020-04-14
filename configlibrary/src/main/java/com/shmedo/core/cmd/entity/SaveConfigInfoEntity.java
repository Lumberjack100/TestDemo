package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.das.cmd.entity
 * 文件名:   SaveConfigInfoEntity
 * 创建者:   dpc
 * 创建时间:  2018/4/18 10:54
 * 描述：    设置保存信息配置
 */

public class SaveConfigInfoEntity  implements Validater {
    private int info;


    public SaveConfigInfoEntity(int info) {
        this.info = info;
    }


    @Override public void validate() {

    }


    @Override public String toString() {
        return String.valueOf(this.info);
    }
}
