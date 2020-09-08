package com.shmedo.core.event;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.event
 * 创建者:   gonghe
 * 创建时间:  2019-10-28
 */
public class BluetoothStateEvent extends MessageEvent {
    public boolean isConnected; //是否连接

    public BluetoothStateEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }


}
