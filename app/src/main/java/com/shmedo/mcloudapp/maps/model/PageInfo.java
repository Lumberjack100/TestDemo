package com.shmedo.mcloudapp.maps.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/19 <br/>
 * 描述：    TODO
 */
public class PageInfo {
    private int page = 0;

    public int getPage(){
        return page;
    }

    public  void nextPage() {
        page++;
    }

    public  void reset() {
        page = 0;
    }

    public  boolean isFirstPage() {
        return page == 0;
    }
}
