package com.shmedo.mcloudapp.entity;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   BasicInfoResult
 * 创建者:   dpc
 * 创建时间:  2019/3/28 11:30
 * 描述：    TODO
 */
public class BasicInfoResult {
    private Integer deviceID;       //设备ID
    private String deviceToken;     //设备token
    private String deviceName;      //设备名称
    private Integer deviceTypeID;   //设备类型ID
    private String deviceTypeName;  //设备类型名称
    private String gpsLocation;     //设置GPS位置 json格式{"lng":121,"lat":31}
    private String installLocation; //设备安装位置说明
    private String securityNO;      //设备解密后的安全码
    private String deviceStatus;    //设备状态
    private Integer projectID;      //设备所属项目ID
    private String projectName;     //设备所属项目名称
    private String desc;            //设备描述
    private String createTime;      //设备添加时间
    private String updateTime;    //设备修改时间
    private String cpuID;           //设备cpuID


    public Integer getDeviceID() {
        return deviceID;
    }


    public void setDeviceID(Integer deviceID) {
        this.deviceID = deviceID;
    }


    public String getDeviceToken() {
        return deviceToken;
    }


    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }


    public String getDeviceName() {
        return deviceName;
    }


    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    public Integer getDeviceTypeID() {
        return deviceTypeID;
    }


    public void setDeviceTypeID(Integer deviceTypeID) {
        this.deviceTypeID = deviceTypeID;
    }


    public String getDeviceTypeName() {
        return deviceTypeName;
    }


    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }


    public String getGpsLocation() {
        return gpsLocation;
    }


    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }


    public String getInstallLocation() {
        return installLocation;
    }


    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }


    public String getSecurityNO() {
        return securityNO;
    }


    public void setSecurityNO(String securityNO) {
        this.securityNO = securityNO;
    }


    public String getDeviceStatus() {
        return deviceStatus;
    }


    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }


    public Integer getProjectID() {
        return projectID;
    }


    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }


    public String getProjectName() {
        return projectName;
    }


    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    public String getDesc() {
        return desc;
    }


    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String getCreateTime() {
        return createTime;
    }


    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }


    public String getUpdateTime() {
        return updateTime;
    }


    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }


    public String getCpuID() {
        return cpuID;
    }


    public void setCpuID(String cpuID) {
        this.cpuID = cpuID;
    }
}
