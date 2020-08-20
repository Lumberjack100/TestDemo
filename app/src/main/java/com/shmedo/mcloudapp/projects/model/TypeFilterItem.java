package com.shmedo.mcloudapp.projects.model;

import com.shmedo.mcloudapp.projects.model.enums.ProjectGroupViewMode;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     按照项目分组方式筛选项目的实体参数类
 */
public class TypeFilterItem extends  FilterItem {
    private ProjectGroupViewMode projectGroupViewMode;

    public TypeFilterItem(String name, ProjectGroupViewMode projectGroupViewMode) {
        super(name);
        this.projectGroupViewMode = projectGroupViewMode;
    }

    public ProjectGroupViewMode getProjectGroupViewMode() {
        return projectGroupViewMode;
    }

    public void setProjectGroupViewMode(ProjectGroupViewMode projectGroupViewMode) {
        this.projectGroupViewMode = projectGroupViewMode;
    }
}
