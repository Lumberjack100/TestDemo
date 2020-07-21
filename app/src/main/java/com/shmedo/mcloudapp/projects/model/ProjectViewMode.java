package com.shmedo.mcloudapp.projects.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     项目浏览模式
 */
public enum ProjectViewMode {
    /**
     * 普通列表模式浏览
     */
    VIEW_SIMPLE,

    /**
     * 按行政区域分组浏览
     */
    VIEW_GROUP_BY_REGION,

    /**
     * 按自定义分级分组浏览
     */
    VIEW_GROUP_BY_LEVEL,

    /**
     * 按项目类型分组浏览
     */
    VIEW_GROUP_BY_TYPE
}
