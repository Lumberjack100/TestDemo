package com.shmedo.core.model;


import com.shmedo.core.enums.ServerAddress;

/**
 * Created by adu on 2017/12/15.
 * 获取服务器地址
 */
public class ServerAddressInfo {
    private ServerAddress port;

    public ServerAddress getPort() {
        return port;
    }

    public void setPort(ServerAddress port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServerAddressInfo{" +
                "port=" + port +
                '}';
    }
}
