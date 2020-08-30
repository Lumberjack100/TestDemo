package com.shmedo.mcloudapp.deviceconfig.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     查询当前公司指令列表接口入参
 */
public class QueryCompanyCmdListParam {

    /**
     * companyID : 1
     * name :
     * deviceTypeID : -1
     * currentPage : 1
     * pageSize : 10
     */

    private int companyID;//当前公司ID
    private String name;//指令名称，支持模糊查询
    private int deviceTypeID;//使用设备类型，-1则不限定
    private int currentPage;//当前页
    private int pageSize;//页大小

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceTypeID() {
        return deviceTypeID;
    }

    public void setDeviceTypeID(int deviceTypeID) {
        this.deviceTypeID = deviceTypeID;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
