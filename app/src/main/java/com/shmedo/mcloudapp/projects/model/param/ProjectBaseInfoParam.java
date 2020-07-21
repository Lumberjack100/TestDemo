package com.shmedo.mcloudapp.projects.model.param;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     TODO
 */
public class ProjectBaseInfoParam {

    /**
     * companyID : null
     * projectName :
     */

    private Object companyID;
    private String projectName;

    public ProjectBaseInfoParam(Object companyID, String projectName) {
        this.companyID = companyID;
        this.projectName = projectName;
    }

    public Object getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Object companyID) {
        this.companyID = companyID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
