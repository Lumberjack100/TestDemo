package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.ServerNumber;

/**
 * Created by adu on 2017/12/19.
 * 设置服务器(数据中心)地址端口（x,y,z之间由空格隔开）
 */
public class ServerAddressInfo {
    private ServerNumber number;
    private String address;
    private int port;

    public ServerNumber getNumber() {
        return number;
    }

    public void setNumber(ServerNumber number) {
        this.number = number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "SetServerAddressPortInfo{" +
                "number=" + number +
                ", address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}
