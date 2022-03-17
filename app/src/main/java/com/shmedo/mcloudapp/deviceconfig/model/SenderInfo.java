package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/16 <br/>
 * 描述：     设备推送信息
 */
public class SenderInfo {
    private int deviceGroupID;//设备组ID
    private String groupName;//设备组名
    private boolean sendEnable;//推送是否启用
    private int sendType;//推送类型code
    private String sendTypeStr;//推送类型描述
    private String sendAddress;//推动地址
    private String sendInfo;//推送附加信息

    public int getDeviceGroupID() {
        return deviceGroupID;
    }

    public void setDeviceGroupID(int deviceGroupID) {
        this.deviceGroupID = deviceGroupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isSendEnable() {
        return sendEnable;
    }

    public void setSendEnable(boolean sendEnable) {
        this.sendEnable = sendEnable;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public String getSendTypeStr() {
        return sendTypeStr;
    }

    public void setSendTypeStr(String sendTypeStr) {
        this.sendTypeStr = sendTypeStr;
    }

    public String getSendAddress() {
        return sendAddress;
    }

    public void setSendAddress(String sendAddress) {
        this.sendAddress = sendAddress;
    }

    public String getSendInfo() {
        return sendInfo;
    }

    public void setSendInfo(String sendInfo) {
        this.sendInfo = sendInfo;
    }
}
