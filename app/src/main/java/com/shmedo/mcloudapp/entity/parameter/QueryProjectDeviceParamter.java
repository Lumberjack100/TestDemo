package com.shmedo.mcloudapp.entity.parameter;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   QueryProjectDeviceParamter
 * 创建者:   dpc
 * 创建时间:  2019/8/19 15:41
 * 描述：    查询项目设备
 */
public class QueryProjectDeviceParamter {

    private int projectID;
    private String deviceName;
    private int pageSize;
    private int currentPage;


    public int getProjectID() {
        return projectID;
    }


    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }


    public String getDeviceName() {
        return deviceName;
    }


    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
}
