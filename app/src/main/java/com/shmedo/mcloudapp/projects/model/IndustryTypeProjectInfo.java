package com.shmedo.mcloudapp.projects.model;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：     行业类型项目信息实体(矿山、国土地质灾害、水文水利、城市基建、其他)
 */
public class IndustryTypeProjectInfo {

    /**
     * projTypeID : 1
     * projTypeName : 矿山
     * projects : [{"projID":200,"shortName":"2019贵州","projName":"2019贵州地灾项目试点","centerPoint":"{\"lng\":\"105.314831\",\"lat\":\"27.318888\"}","top":null},{"projID":432,"shortName":"贵州地灾","projName":"贵州地灾","centerPoint":"{\"lng\":\"113.2222\",\"lat\":\"22.222\"}","top":null},{"projID":433,"shortName":"贵州毕节市地","projName":"贵州毕节市地灾","centerPoint":"{\"lng\":\"123.1\",\"lat\":\"23.1\"}","top":null}]
     */

    private int projTypeID;
    private String projTypeName;
    private List<ProjectBaseInfo> projects;

    public int getProjTypeID() {
        return projTypeID;
    }

    public void setProjTypeID(int projTypeID) {
        this.projTypeID = projTypeID;
    }

    public String getProjTypeName() {
        return projTypeName;
    }

    public void setProjTypeName(String projTypeName) {
        this.projTypeName = projTypeName;
    }

    public List<ProjectBaseInfo> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectBaseInfo> projects) {
        this.projects = projects;
    }

}
