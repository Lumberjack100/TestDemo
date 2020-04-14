package com.shmedo.core.model;


import com.shmedo.core.enums.SetServerAddressPort;

/**
 * Created by adu on 2017/12/19.
 * 设置服务器地址端口（x,y,z之间由空格隔开）
 */
public class SetServerAddressPortInfo {
    private SetServerAddressPort number;
    private String address;
    private int port;

    public SetServerAddressPort getNumber() {
        return number;
    }

    public void setNumber(SetServerAddressPort number) {
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
