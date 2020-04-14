package com.shmedo.core.model;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   SaveConfigInfo
 * 创建者:   dpc
 * 创建时间:  2018/4/18 10:52
 * 描述：    保存配置信息
 */

public class SaveConfigInfo {
    private int info;


    public int getInfo() {
        return info;
    }


    public void setInfo(int info) {
        this.info = info;
    }


    @Override public String toString() {
        return "SaveConfigInfo{" +
            "info=" + info +
            '}';
    }
}
