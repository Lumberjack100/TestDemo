package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/21 <br/>
 * 描述：     数据分页信息
 */
public class PageInfo {
    private int page = 0;
    private int firstPage = 0;

    public PageInfo() {
    }

    public PageInfo(int firstPage) {
        this.firstPage = firstPage;
        this.page = firstPage;
    }

    public void nextPage() {
        page++;
    }

    public void reset() {
        page = firstPage;
    }

    public int getPage() {
        return page;
    }

    public boolean isFirstPage() {
        return page == firstPage;
    }
}
