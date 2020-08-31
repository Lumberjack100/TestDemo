package com.shmedo.mcloudapp.deviceconfig.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：      查询设备状态历史入参
 */
public class QueryCmdStateParam {

    /**
     * companyID : 1
     * deviceID : 1347
     * begin : 2020-08-24 11:28:03
     * end : 2020-08-31 11:28:03
     * pageSize : 5
     * currentPage : 1
     */

    private int companyID;//当前公司ID
    private int deviceID;//设备ID
    private String begin;//开始时间
    private String end;//结束时间
    private int pageSize;//页大小
    private int currentPage;//当前页

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
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
