package com.shmedo.mcloudapp.entity;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   ProjectDeviceInfoList
 * 创建者:   dpc
 * 创建时间:  2019/8/19 14:20
 *
 */
public class PageResult<T> {
    private int totalPage;
    private int totalCount;
    private List<T> currentPageData;


    public int getTotalPage() {
        return totalPage;
    }


    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }


    public int getTotalCount() {
        return totalCount;
    }


    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }


    public List<T> getCurrentPageData() {
        return currentPageData;
    }


    public void setCurrentPageData(List<T> currentPageData) {
        this.currentPageData = currentPageData;
    }
}
