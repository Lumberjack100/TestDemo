package com.shmedo.mcloudapp.entity.parameter;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   AllDeviceParameter
 * 创建者:   dpc
 * 创建时间:  2019/4/15 17:37
 * 描述：    所有设备需要上传的参数
 */
public class AllDeviceParameter {
    private int deviceType;
    private String name;
    private int pageSize;
    private int currentPage;


    public int getDeviceType() {
        return deviceType;
    }


    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
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
