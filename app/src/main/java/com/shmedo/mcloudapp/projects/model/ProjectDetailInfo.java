package com.shmedo.mcloudapp.projects.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/20 <br/>
 * 描述：     TODO
 */
@Entity
public class ProjectDetailInfo {

    /**
     * projectID : 132
     * shortName : E60/DA
     * projectName : E60/DAS设备测试
     * buildTime : 2018-08-06 11:28:01
     * imagePath : null
     * companyID : 1
     * companyName : 上海米度测控科技有限公司
     * projectTypeID : 3
     * projectTypeName : 国土地质灾害
     * regionID : 805
     * regionFullName : 上海市-上海市市辖区-浦东新区
     * warnStatus : 2
     * lastMonthUnhandleWarn : 529
     * openedMonitorType : [{"id":66,"sensorType":0,"typeName":"地表位移","name":"地表位移","displayOrder":0,"exValues":"你好12","iconImage":"medo-icon-sensortype-0","allowMultiSensorInPoint":false,"isValid":true},{"id":68,"sensorType":2,"typeName":"内部位移","name":"内部位移","displayOrder":2,"exValues":"12","iconImage":"medo-icon-sensortype-2","allowMultiSensorInPoint":true,"isValid":true},{"id":69,"sensorType":3,"typeName":"雨量","name":"雨量","displayOrder":3,"exValues":null,"iconImage":"medo-icon-sensortype-3","allowMultiSensorInPoint":false,"isValid":true},{"id":70,"sensorType":4,"typeName":"水位","name":"库水位","displayOrder":4,"exValues":null,"iconImage":"medo-icon-sensortype-4","allowMultiSensorInPoint":false,"isValid":true},{"id":71,"sensorType":5,"typeName":"干滩","name":"干滩","displayOrder":5,"exValues":null,"iconImage":"medo-icon-sensortype-5","allowMultiSensorInPoint":false,"isValid":true},{"id":72,"sensorType":6,"typeName":"温度","name":"温度","displayOrder":6,"exValues":null,"iconImage":"medo-icon-sensortype-6","allowMultiSensorInPoint":false,"isValid":true},{"id":73,"sensorType":7,"typeName":"浸润线","name":"浸润线","displayOrder":7,"exValues":null,"iconImage":"medo-icon-sensortype-7","allowMultiSensorInPoint":false,"isValid":true},{"id":74,"sensorType":8,"typeName":"倾角","name":"倾角计","displayOrder":8,"exValues":null,"iconImage":"medo-icon-sensortype-8","allowMultiSensorInPoint":false,"isValid":true},{"id":75,"sensorType":9,"typeName":"沉降","name":"静力水准","displayOrder":9,"exValues":null,"iconImage":"medo-icon-sensortype-9","allowMultiSensorInPoint":false,"isValid":true},{"id":76,"sensorType":10,"typeName":"视频","name":"视频","displayOrder":10,"exValues":null,"iconImage":"medo-icon-sensortype-10","allowMultiSensorInPoint":false,"isValid":true},{"id":77,"sensorType":11,"typeName":"流量流速","name":"量水堰","displayOrder":11,"exValues":null,"iconImage":"medo-icon-sensortype-11","allowMultiSensorInPoint":false,"isValid":true},{"id":79,"sensorType":13,"typeName":"浊度","name":"浊度仪","displayOrder":13,"exValues":null,"iconImage":"medo-icon-sensortype-13","allowMultiSensorInPoint":false,"isValid":true},{"id":80,"sensorType":14,"typeName":"裂缝","name":"裂缝","displayOrder":14,"exValues":null,"iconImage":"medo-icon-sensortype-14","allowMultiSensorInPoint":false,"isValid":true},{"id":81,"sensorType":15,"typeName":"土壤含水率","name":"土壤含水率","displayOrder":15,"exValues":null,"iconImage":"medo-icon-sensortype-15","allowMultiSensorInPoint":true,"isValid":true},{"id":82,"sensorType":16,"typeName":"爆破震动","name":"爆破震动","displayOrder":16,"exValues":null,"iconImage":"medo-icon-sensortype-16","allowMultiSensorInPoint":false,"isValid":true},{"id":83,"sensorType":17,"typeName":"多点位移","name":"多点位移","displayOrder":17,"exValues":null,"iconImage":"medo-icon-sensortype-17","allowMultiSensorInPoint":true,"isValid":true},{"id":85,"sensorType":20,"typeName":"湿度","name":"湿度","displayOrder":20,"exValues":null,"iconImage":"medo-icon-sensortype-20","allowMultiSensorInPoint":false,"isValid":true},{"id":86,"sensorType":21,"typeName":"土压力","name":"土压力","displayOrder":21,"exValues":null,"iconImage":"medo-icon-sensortype-21","allowMultiSensorInPoint":false,"isValid":true},{"id":87,"sensorType":22,"typeName":"应力应变","name":"应变计","displayOrder":22,"exValues":null,"iconImage":"medo-icon-sensortype-22","allowMultiSensorInPoint":false,"isValid":true},{"id":88,"sensorType":23,"typeName":"支撑轴力","name":"支撑轴力","displayOrder":23,"exValues":null,"iconImage":"medo-icon-sensortype-23","allowMultiSensorInPoint":true,"isValid":true},{"id":89,"sensorType":24,"typeName":"PH","name":"PH计","displayOrder":24,"exValues":null,"iconImage":"medo-icon-sensortype-24","allowMultiSensorInPoint":false,"isValid":true},{"id":91,"sensorType":28,"typeName":"孔隙水压力","name":"孔隙水压力","displayOrder":100,"exValues":null,"iconImage":"medo-icon-sensortype-28","allowMultiSensorInPoint":false,"isValid":true}]
     * centerPoint : {"lng":"121.612447","lat":"31.193943"}
     * location : null
     * registerTime : 2020-01-01 00:00:00
     * isValid : true
     */

    @Id
    private Long id;//添加在 sqllite 中作为主键
    private int userId;//添加在 sqllite 中，标识用户
    private boolean top;//项目是否置顶

    @Unique
    private int projectID;//项目ID
    private String shortName;//项目短名称
    private String projectName;//项目名称
    private String buildTime;//项目建立时间
    private String imagePath;//缩略图地址
    private int companyID;//项目所属公司ID
    private String companyName;//项目所属公司名称
    private int projectTypeID;//项目类型ID
    private String projectTypeName;//项目类型名称
    private int regionID;//所属行政区域ID
    private String regionFullName;//所属行政区域名称
    private int warnStatus;//设置的预警状态
    private int lastMonthUnhandleWarn;//当前一个月内未处理警报数量
    private String centerPoint;//项目中心坐标
    private String location;//项目地址
    private String registerTime;//项目注册有效期
    private boolean isValid;//项目状态：在线，离线

    @Transient
    private List<OpenedMonitorTypeBean> openedMonitorType;//项目开启的监测类型
    private boolean isOutOfDate = false;//是否过期

    @Generated(hash = 1343148853)
    public ProjectDetailInfo(Long id, int userId, boolean top, int projectID, String shortName, String projectName, String buildTime, String imagePath, int companyID, String companyName, int projectTypeID, String projectTypeName, int regionID, String regionFullName, int warnStatus, int lastMonthUnhandleWarn, String centerPoint, String location, String registerTime, boolean isValid, boolean isOutOfDate) {
        this.id = id;
        this.userId = userId;
        this.top = top;
        this.projectID = projectID;
        this.shortName = shortName;
        this.projectName = projectName;
        this.buildTime = buildTime;
        this.imagePath = imagePath;
        this.companyID = companyID;
        this.companyName = companyName;
        this.projectTypeID = projectTypeID;
        this.projectTypeName = projectTypeName;
        this.regionID = regionID;
        this.regionFullName = regionFullName;
        this.warnStatus = warnStatus;
        this.lastMonthUnhandleWarn = lastMonthUnhandleWarn;
        this.centerPoint = centerPoint;
        this.location = location;
        this.registerTime = registerTime;
        this.isValid = isValid;
        this.isOutOfDate = isOutOfDate;
    }

    @Generated(hash = 950442219)
    public ProjectDetailInfo() {
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getProjectTypeID() {
        return projectTypeID;
    }

    public void setProjectTypeID(int projectTypeID) {
        this.projectTypeID = projectTypeID;
    }

    public String getProjectTypeName() {
        return projectTypeName;
    }

    public void setProjectTypeName(String projectTypeName) {
        this.projectTypeName = projectTypeName;
    }

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }

    public String getRegionFullName() {
        return regionFullName;
    }

    public void setRegionFullName(String regionFullName) {
        this.regionFullName = regionFullName;
    }

    public int getWarnStatus() {
        return warnStatus;
    }

    public void setWarnStatus(int warnStatus) {
        this.warnStatus = warnStatus;
    }

    public int getLastMonthUnhandleWarn() {
        return lastMonthUnhandleWarn;
    }

    public void setLastMonthUnhandleWarn(int lastMonthUnhandleWarn) {
        this.lastMonthUnhandleWarn = lastMonthUnhandleWarn;
    }

    public String getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(String centerPoint) {
        this.centerPoint = centerPoint;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }

    public boolean isIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean isOutOfDate() {
        return isOutOfDate;
    }

    public void setOutOfDate(boolean outOfDate) {
        isOutOfDate = outOfDate;
    }

    public List<OpenedMonitorTypeBean> getOpenedMonitorType() {
        return openedMonitorType;
    }

    public void setOpenedMonitorType(List<OpenedMonitorTypeBean> openedMonitorType) {
        this.openedMonitorType = openedMonitorType;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean getIsValid() {
        return this.isValid;
    }

    public boolean getIsOutOfDate() {
        return this.isOutOfDate;
    }

    public void setIsOutOfDate(boolean isOutOfDate) {
        this.isOutOfDate = isOutOfDate;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public boolean getTop() {
        return this.top;
    }
}
