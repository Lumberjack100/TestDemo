package com.shmedo.mcloudapp.deviceconfig.model;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/15 <br/>
 * 描述：     设备详细信息接口返回的设备信息实体
 */
public class DetailDeviceInfo {

    private DeviceBaseInfo deviceBaseInfo;
    private List<DeviceTag> tagList;
    private List<SimInfo> simDetailList;
    private List<SenderInfo> senderInfoList;

    public DeviceBaseInfo getDeviceBaseInfo() {
        return deviceBaseInfo;
    }

    public void setDeviceBaseInfo(DeviceBaseInfo deviceBaseInfo) {
        this.deviceBaseInfo = deviceBaseInfo;
    }

    public List<DeviceTag> getTagList() {
        return tagList;
    }

    public void setTagList(List<DeviceTag> tagList) {
        this.tagList = tagList;
    }

    public List<SimInfo> getSimDetailList() {
        return simDetailList;
    }

    public void setSimDetailList(List<SimInfo> simDetailList) {
        this.simDetailList = simDetailList;
    }

    public List<SenderInfo> getSenderInfoList() {
        return senderInfoList;
    }

    public void setSenderInfoList(List<SenderInfo> senderInfoList) {
        this.senderInfoList = senderInfoList;
    }
}
