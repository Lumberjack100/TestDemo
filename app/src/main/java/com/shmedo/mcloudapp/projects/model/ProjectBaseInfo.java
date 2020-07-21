package com.shmedo.mcloudapp.projects.model;

import java.io.Serializable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/20 <br/>
 * 描述：     项目基本信息实体
 */
public class ProjectBaseInfo  implements Serializable {

    /**
     * projID : 379
     * shortName : 云锦路ADM
     * projName : 云锦路ADME项目
     * centerPoint : {"lng":"121.464914","lat":"31.17265"}
     * top : false
     */

    private int projID;//项目ID
    private String shortName;//项目短名称
    private String projName;//项目名称
    private String centerPoint;//项目中心坐标
    private boolean top;//项目是否置顶

    public int getProjID() {
        return projID;
    }

    public void setProjID(int projID) {
        this.projID = projID;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getProjName() {
        return projName;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public String getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(String centerPoint) {
        this.centerPoint = centerPoint;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }
}
