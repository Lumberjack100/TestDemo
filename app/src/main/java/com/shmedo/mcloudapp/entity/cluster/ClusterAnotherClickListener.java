package com.shmedo.mcloudapp.entity.cluster;

import com.amap.api.maps.model.Marker;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.cluster
 * 文件名:   ClusterAnotherClickListener
 * 创建者:   dpc
 * 创建时间:  2019/8/14 11:30
 *
 */
public interface ClusterAnotherClickListener {
    /**
     * 点击聚合点的回调处理函数
     *
     * @param marker
     *            点击的聚合点
     * @param clusterItems
     *            聚合点所包含的元素
     */
    public void onAnotherClick(Marker marker, List<ClusterItem> clusterItems);
}
