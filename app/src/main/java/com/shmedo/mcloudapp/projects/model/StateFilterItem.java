package com.shmedo.mcloudapp.projects.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     TODO
 */
public class StateFilterItem extends  FilterItem {
    private ProjectFilterScope filterScope;

    public StateFilterItem(String name, ProjectFilterScope filterScope) {
        super(name);
        this.filterScope = filterScope;
    }

    public ProjectFilterScope getFilterScope() {
        return filterScope;
    }

    public void setFilterScope(ProjectFilterScope filterScope) {
        this.filterScope = filterScope;
    }
}
