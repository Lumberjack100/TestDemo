package com.shmedo.mcloudapp.projects.model;

import com.shmedo.mcloudapp.projects.model.enums.ProjectState;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：    按照项目状态筛选项目的实体参数类
 */
public class StateFilterItem extends  FilterItem {
    private ProjectState filterScope;

    public StateFilterItem(String name, ProjectState filterScope) {
        super(name);
        this.filterScope = filterScope;
    }

    public ProjectState getFilterScope() {
        return filterScope;
    }

    public void setFilterScope(ProjectState filterScope) {
        this.filterScope = filterScope;
    }
}
