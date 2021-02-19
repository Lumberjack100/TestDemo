package com.shmedo.mcloudapp.projects.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：     TODO #gh#
 */
public class ProjectItem implements MultiItemEntity {
    public static final int ITEM_TOP = 1;
    public static final int ITEM_MIDDLE = 2;
    public static final int ITEM_BOTTOM = 3;
    private Object object;
    private int itemType;

    public ProjectItem(Object object) {
        this.object = object;
    }

    public ProjectItem(Object object, int itemType) {
        this.object = object;
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public Object getObject() {
        return object;
    }
}
