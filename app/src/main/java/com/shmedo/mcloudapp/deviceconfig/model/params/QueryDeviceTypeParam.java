package com.shmedo.mcloudapp.deviceconfig.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/15 <br/>
 * 描述：    查询设备类型接口入参
 */
public class QueryDeviceTypeParam {

    /**
     * deviceTypeName : DAS
     * pageSize : 20
     * currentPage : 1
     */

    private String deviceTypeName;
    private int pageSize;
    private int currentPage;

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
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
