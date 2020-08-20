package com.shmedo.mcloudapp.projects.model;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：    自定义分级项目信息实体
 */
public class CustomLevelProjectInfo {

    /**
     * levelID : -1
     * fatherLevelID : -1
     * levelName : 未分级项目
     * levelProjs : [{"projID":132,"shortName":"E60/DA","projName":"E60/DAS设备测试"},{"projID":180,"shortName":"云南昭通大关","projName":"云南昭通大关悦乐姜家坪子不稳定斜坡监测"},{"projID":200,"shortName":"2019贵州","projName":"2019贵州地灾项目试点"},{"projID":379,"shortName":"云锦路ADM","projName":"云锦路ADME项目"},{"projID":380,"shortName":"米度ADME","projName":"米度ADME测试项目"},{"projID":432,"shortName":"贵州地灾","projName":"贵州地灾"},{"projID":433,"shortName":"贵州毕节市地","projName":"贵州毕节市地灾"}]
     * subLevels : null
     */

    private int levelID;
    private int fatherLevelID;
    private String levelName;
    private Object subLevels;
    private List<ProjectBaseInfo> levelProjs;

    public int getLevelID() {
        return levelID;
    }

    public void setLevelID(int levelID) {
        this.levelID = levelID;
    }

    public int getFatherLevelID() {
        return fatherLevelID;
    }

    public void setFatherLevelID(int fatherLevelID) {
        this.fatherLevelID = fatherLevelID;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Object getSubLevels() {
        return subLevels;
    }

    public void setSubLevels(Object subLevels) {
        this.subLevels = subLevels;
    }

    public List<ProjectBaseInfo> getLevelProjs() {
        return levelProjs;
    }

    public void setLevelProjs(List<ProjectBaseInfo> levelProjs) {
        this.levelProjs = levelProjs;
    }

}
