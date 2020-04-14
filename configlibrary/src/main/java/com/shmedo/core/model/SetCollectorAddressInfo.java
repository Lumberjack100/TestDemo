package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/19.
 * 设置采集器地址
 */
public class SetCollectorAddressInfo {
    private int address;

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "SetCollectorAddressInfo{" +
                "address=" + address +
                '}';
    }
}
