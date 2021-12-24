package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     查询指令结果返回实体
 */
public class QueryCmdResult {


    private String msgID;
    private int deviceID;
    private String deviceSN;
    private String cmdEngName;
    private String cmdChnName;
    private String cmdContent;
    private String dispatchTime;
    private int cmdStatus;
    private String cmdStatusString;
    private String responseTime;
    private String responseContent;
    private int responseStatus;
    private String responseStatusString;
    private int dispatchUserID;
    private String dispatchUserName;
    private String cmdID;

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getCmdEngName() {
        return cmdEngName;
    }

    public void setCmdEngName(String cmdEngName) {
        this.cmdEngName = cmdEngName;
    }

    public String getCmdChnName() {
        return cmdChnName;
    }

    public void setCmdChnName(String cmdChnName) {
        this.cmdChnName = cmdChnName;
    }

    public String getCmdContent() {
        return cmdContent;
    }

    public void setCmdContent(String cmdContent) {
        this.cmdContent = cmdContent;
    }

    public String getDispatchTime() {
        return dispatchTime;
    }

    public void setDispatchTime(String dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    public int getCmdStatus() {
        return cmdStatus;
    }

    public void setCmdStatus(int cmdStatus) {
        this.cmdStatus = cmdStatus;
    }

    public String getCmdStatusString() {
        return cmdStatusString;
    }

    public void setCmdStatusString(String cmdStatusString) {
        this.cmdStatusString = cmdStatusString;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseStatusString() {
        return responseStatusString;
    }

    public void setResponseStatusString(String responseStatusString) {
        this.responseStatusString = responseStatusString;
    }

    public int getDispatchUserID() {
        return dispatchUserID;
    }

    public void setDispatchUserID(int dispatchUserID) {
        this.dispatchUserID = dispatchUserID;
    }

    public String getDispatchUserName() {
        return dispatchUserName;
    }

    public void setDispatchUserName(String dispatchUserName) {
        this.dispatchUserName = dispatchUserName;
    }

    public String getCmdID() {
        return cmdID;
    }

    public void setCmdID(String cmdID) {
        this.cmdID = cmdID;
    }
}
