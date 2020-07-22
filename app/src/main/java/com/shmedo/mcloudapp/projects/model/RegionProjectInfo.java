package com.shmedo.mcloudapp.projects.model;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：     TODO
 */
public class RegionProjectInfo {

    /**
     * regionID : 796
     * regionFullName : 上海市-上海市市辖区-徐汇区
     * projects : [{"projID":379,"shortName":"云锦路ADM","projName":"云锦路ADME项目","centerPoint":"{\"lng\":\"121.464914\",\"lat\":\"31.17265\"}","top":null}]
     */

    private int regionID;
    private String regionFullName;
    private List<ProjectBaseInfo> projects;

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }

    public String getRegionFullName() {
        return regionFullName;
    }

    public void setRegionFullName(String regionFullName) {
        this.regionFullName = regionFullName;
    }

    public List<ProjectBaseInfo> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectBaseInfo> projects) {
        this.projects = projects;
    }

}
