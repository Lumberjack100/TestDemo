package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     指令下发/透传接口返回实体类
 */
public class DispatchCmdItem {

    /**
     * deviceID : 1262
     * msgID : 1a7ea836-b53c-4ea4-b508-455de9904135
     */

    private int deviceID;//设备ID
    private String msgID;//消息ID,凭借该ID查询该指令的响应结果

    public int getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }
}
