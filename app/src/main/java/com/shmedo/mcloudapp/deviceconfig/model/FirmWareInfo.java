package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/1 <br/>
 * 描述：     设备固件信息
 */
public class FirmWareInfo {

    /**
     * id : 19
     * fwName : DAS_LDS08_NET
     * fwVersion : 3.0.7
     * fwSize : 174657
     * fwMd5 : 38216ca54633e6c4bf5cb889030c4c02
     * fwNote : 1.支持MQTT设备管理平台，远程升级功能
     2.修改雨量计精度默认参数为0.2mm
     3.修改地大平台查询雨量计类型返回003_9的问题，应该是003_0
     * fwPath : https://mdnetfile.shmedo.cn/202001/c0bf5180-0dcf-4d83-957d-d3b746f3f81d.bin
     * uploadUserID : 514
     * uploadUserName : 杨启帆
     * uploadTime : 2020-01-03 12:29:36
     * deviceTypeID : 4
     * deviceTypeName : DAS
     */

    private int id;
    private String fwName;
    private String fwVersion;
    private int fwSize;
    private String fwMd5;
    private String fwNote;
    private String fwPath;
    private int uploadUserID;
    private String uploadUserName;
    private String uploadTime;
    private int deviceTypeID;
    private String deviceTypeName;

    private boolean isChecked = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFwName() {
        return fwName;
    }

    public void setFwName(String fwName) {
        this.fwName = fwName;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public int getFwSize() {
        return fwSize;
    }

    public void setFwSize(int fwSize) {
        this.fwSize = fwSize;
    }

    public String getFwMd5() {
        return fwMd5;
    }

    public void setFwMd5(String fwMd5) {
        this.fwMd5 = fwMd5;
    }

    public String getFwNote() {
        return fwNote;
    }

    public void setFwNote(String fwNote) {
        this.fwNote = fwNote;
    }

    public String getFwPath() {
        return fwPath;
    }

    public void setFwPath(String fwPath) {
        this.fwPath = fwPath;
    }

    public int getUploadUserID() {
        return uploadUserID;
    }

    public void setUploadUserID(int uploadUserID) {
        this.uploadUserID = uploadUserID;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public void setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getDeviceTypeID() {
        return deviceTypeID;
    }

    public void setDeviceTypeID(int deviceTypeID) {
        this.deviceTypeID = deviceTypeID;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
