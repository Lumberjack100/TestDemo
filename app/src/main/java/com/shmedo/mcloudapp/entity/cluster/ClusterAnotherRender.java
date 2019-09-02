package com.shmedo.mcloudapp.entity.cluster;

import android.graphics.drawable.Drawable;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.cluster
 * 文件名:   ClusterAnotherRender
 * 创建者:   dpc
 * 创建时间:  2019/8/14 11:31
 * 描述：    TODO
 */
public interface ClusterAnotherRender {
    /**
     * 根据聚合点的元素数目返回渲染背景样式
     *
     * @param clusterNum
     * @return
     */
    Drawable getAnotherDrawAble(int clusterNum);
}
