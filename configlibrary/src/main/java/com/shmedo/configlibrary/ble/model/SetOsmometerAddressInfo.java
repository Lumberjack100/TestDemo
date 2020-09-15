package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2018/1/8.
 * 设置数字渗压地址实体类；
 */
public class SetOsmometerAddressInfo {
    private int address;

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "SetOsmometerAddressInfo{" +
                "address=" + address +
                '}';
    }
}
