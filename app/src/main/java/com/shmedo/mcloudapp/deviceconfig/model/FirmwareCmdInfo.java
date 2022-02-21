package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/21 <br/>
 * 描述：     TODO
 */
public class FirmwareCmdInfo {

    private String deviceToken;
    private String msgID;

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }
}
