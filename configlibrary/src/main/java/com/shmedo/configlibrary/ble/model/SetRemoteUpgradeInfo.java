package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.SetRemoteUpgrade;

/**
 * Created by adu on 2017/12/18.
 * 设置远程升级的实体类
 */
public class SetRemoteUpgradeInfo {
    private SetRemoteUpgrade model;
    private int port;

    public SetRemoteUpgrade getModel() {
        return model;
    }

    public void setModel(SetRemoteUpgrade model) {
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
