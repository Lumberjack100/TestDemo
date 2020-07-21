package com.shmedo.mcloudapp.projects.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     TODO
 */
public class TypeFilterItem extends  FilterItem {
    private ProjectViewMode projectViewMode;

    public TypeFilterItem(String name, ProjectViewMode projectViewMode) {
        super(name);
        this.projectViewMode = projectViewMode;
    }

    public ProjectViewMode getProjectViewMode() {
        return projectViewMode;
    }

    public void setProjectViewMode(ProjectViewMode projectViewMode) {
        this.projectViewMode = projectViewMode;
    }
}
