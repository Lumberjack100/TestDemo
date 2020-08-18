package com.shmedo.mcloudapp.projects.model;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：     TODO
 */
public class ProjectDeviceInfoWrapper {

    /**
     * totalCount : 383
     * totalPage : 20
     * currentPageData : [{"id":181,"token":"180706K","name":"180706K","deviceTypeID":5,"deviceTypeName":"DAG","gpsLocation":"","installLocation":"","deviceStatus":"启用","desc":"","projectID":380,"projectName":"米度ADME测试项目","online":false,"registerCompanyID":1,"registerCompanyName":"上海米度测控科技有限公司","deviceTag":"测试","lastActiveTime":"2020-01-02 15:49:02","firmwareVersion":null,"deviceSimList":[{"simID":29,"ccid":"89860445101970723691","simNO":"1440456841134","simIsp":"中国移动","vendor":"浙江企朋"}]}]
     */

    private int totalCount;
    private int totalPage;
    private List<ProjectDeviceInfo> currentPageData;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<ProjectDeviceInfo> getCurrentPageData() {
        return currentPageData;
    }

    public void setCurrentPageData(List<ProjectDeviceInfo> currentPageData) {
        this.currentPageData = currentPageData;
    }

}
