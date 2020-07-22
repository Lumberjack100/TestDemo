package com.shmedo.mcloudapp.projects.model;

import com.chad.library.adapter.base.entity.JSectionEntity;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：     TODO
 */
public class ProjectItem extends JSectionEntity {
    private boolean isHeader;
    private Object  object;

    public ProjectItem(boolean isHeader, Object object) {
        this.isHeader = isHeader;
        this.object = object;
    }

    @Override
    public boolean isHeader() {
        return isHeader;
    }

    public Object getObject() {
        return object;
    }
}
