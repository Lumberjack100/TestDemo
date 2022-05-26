package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/5/24 <br/>
 * 描述：     TODO
 */
public class DeviceDebugAddress {

    private DeviceServerInfoInfo deviceServerInfo;
    private DeviceServerInfoInfo clientServerInfo;
    private String deviceSn;
    private String lastActiveDateTime;

    public DeviceServerInfoInfo getDeviceServerInfo() {
        return deviceServerInfo;
    }

    public void setDeviceServerInfo(DeviceServerInfoInfo deviceServerInfo) {
        this.deviceServerInfo = deviceServerInfo;
    }

    public DeviceServerInfoInfo getClientServerInfo() {
        return clientServerInfo;
    }

    public void setClientServerInfo(DeviceServerInfoInfo clientServerInfo) {
        this.clientServerInfo = clientServerInfo;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getLastActiveDateTime() {
        return lastActiveDateTime;
    }

    public void setLastActiveDateTime(String lastActiveDateTime) {
        this.lastActiveDateTime = lastActiveDateTime;
    }
}
