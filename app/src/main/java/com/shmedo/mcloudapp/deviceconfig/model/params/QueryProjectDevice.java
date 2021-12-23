package com.shmedo.mcloudapp.deviceconfig.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：    查询项目下设备参数类
 */
public class QueryProjectDevice {

    /**
     * companyID : 1
     * deviceType : -1
     * sn :
     * registerCompanyName :
     * projectName :
     * deviceStatus :
     * deviceTag :
     * pageSize : 20
     * currentPage : 1
     * orderField :
     * orderString :
     */

    private int companyID;
    private int deviceType;
    private String sn;
    private String registerCompanyName;
    private String projectName;
    private String deviceStatus;
    private String deviceTag;
    private int pageSize;
    private int currentPage;
    private String orderField;
    private String orderString;

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getRegisterCompanyName() {
        return registerCompanyName;
    }

    public void setRegisterCompanyName(String registerCompanyName) {
        this.registerCompanyName = registerCompanyName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceTag() {
        return deviceTag;
    }

    public void setDeviceTag(String deviceTag) {
        this.deviceTag = deviceTag;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public String getOrderString() {
        return orderString;
    }

    public void setOrderString(String orderString) {
        this.orderString = orderString;
    }
}
