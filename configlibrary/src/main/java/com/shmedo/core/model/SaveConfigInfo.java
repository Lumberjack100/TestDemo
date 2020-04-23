package com.shmedo.core.model;

import com.shmedo.core.enums.SaveConfigMode;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SaveConfigInfo
 * 创建者:   dpc
 * 创建时间:  2018/4/18 10:52
 * 描述：    保存配置信息
 */

public class SaveConfigInfo {
    private SaveConfigMode mode;

    public SaveConfigMode getMode() {
        return mode;
    }

    public void setMode(SaveConfigMode mode) {
        this.mode = mode;
    }

    @Override public String toString() {
        return "SaveConfigInfo{" +
            "mode=" + mode +
            '}';
    }
}
