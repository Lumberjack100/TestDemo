package com.shmedo.core.event;


import com.shmedo.core.util.NetworkUtils;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 创建者:   gonghe
 * 创建时间:  2019-10-18
 *
 */
public class NetworkChangeEvent extends MessageEvent{
    public boolean isConnected; //是否存在网络

    public NetworkUtils.NetworkType networkType;


    public NetworkChangeEvent(boolean isConnected, NetworkUtils.NetworkType networkType) {
        this.isConnected = isConnected;
        this.networkType = networkType;
    }
}
