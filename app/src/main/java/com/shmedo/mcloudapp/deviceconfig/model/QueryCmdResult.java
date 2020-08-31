package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     查询指令结果返回实体
 */
public class QueryCmdResult {

    /**
     * deviceID : 1262
     * deviceSN : 110007L
     * cmdEngName : sample
     * cmdChnName : 传感器遥测
     * cmdContent : $cmd=sample
     * dispatchTime : 2020-08-30 11:11:37
     * cmdStatus : 1
     * cmdStatusString : 已下发等待响应
     * responseTime : null
     * responseContent : null
     * dispatchUserID : 507
     * dispatchUserName : 宫加贝
     */

    private int deviceID;//设备ID
    private String deviceSN;//设备SN
    private String cmdEngName;//指令英文名称
    private String cmdChnName;//指令中文名称
    private String cmdContent;//指令内容
    private String dispatchTime;//下发时间
    private int cmdStatus;//响应状态
    private String cmdStatusString;//响应状态文字
    private String responseTime;//响应时间
    private String responseContent;//响应内容
    private int dispatchUserID;//下发用户ID
    private String dispatchUserName;//下发用户名称

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
}
