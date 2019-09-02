package com.shmedo.mcloudapp.entity.cluster;

import com.amap.api.maps.model.LatLng;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.cluster
 * 文件名:   ClusterItem
 * 创建者:   dpc
 * 创建时间:  2019/8/13 19:42
 * 描述：    TODO
 */
public interface ClusterItem {
    /**
     * 返回聚合元素的地理位置
     *
     * @return
     */
    LatLng getPosition();

    /**
     * 获取聚合点的类型
     * @return
     */
    String getClusterType();
}
