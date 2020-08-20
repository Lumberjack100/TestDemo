package com.shmedo.mcloudapp.projects.model.enums;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     项目分组浏览方式
 */
public enum ProjectGroupViewMode {
    /**
     * 不分组，普通列表模式浏览
     */
    SIMPLE_LIST,

    /**
     * 按行政区域分组浏览
     */
    GROUP_BY_REGION,

    /**
     * 按自定义分级分组浏览
     */
    GROUP_BY_LEVEL,

    /**
     * 按项目类型分组浏览
     */
    GROUP_BY_TYPE
}
