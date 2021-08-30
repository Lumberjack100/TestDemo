package com.shmedo.mcloudapp.deviceconfig.model.usb_serial;

import android.hardware.usb.UsbDevice;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/10 <br/>
 * 描述：     USB 设备列表项实体
 */
public class USBDeviceItem {
    private UsbDevice device;
    private int port;
    private UsbSerialDriver driver;

    public USBDeviceItem(UsbDevice device, int port, UsbSerialDriver driver) {
        this.device = device;
        this.port = port;
        this.driver = driver;
    }

    public UsbDevice getDevice() {
        return device;
    }

    public void setDevice(UsbDevice device) {
        this.device = device;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public UsbSerialDriver getDriver() {
        return driver;
    }

    public void setDriver(UsbSerialDriver driver) {
        this.driver = driver;
    }
}
