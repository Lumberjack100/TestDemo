package com.shmedo.core.model;


import com.shmedo.core.enums.SettingRemoteUpgrade;

/**
 * Created by adu on 2017/12/18.
 * 设置远程升级的实体类
 */
public class SettingRemoteUpgradeInfo {
    private SettingRemoteUpgrade model;
    private int port;

    public SettingRemoteUpgrade getModel() {
        return model;
    }

    public void setModel(SettingRemoteUpgrade model) {
        this.model = model;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "SettingRemoteUpgradeInfo{" +
                "model=" + model +
                ", port=" + port +
                '}';
    }
}
