package com.shmedo.mcloudapp.entity.cluster;

import com.amap.api.maps.model.LatLng;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   ClusterItemImp
 * 创建者:   dpc
 * 创建时间:  2019/8/13 19:41
 *
 */
public class ClusterItemImp implements ClusterItem{
    private LatLng latLng;
    private String clusterType;

    public ClusterItemImp(LatLng latLng, String clusterType) {
        this.latLng = latLng;
        this.clusterType = clusterType;
    }

    @Override public LatLng getPosition() {
        return latLng;
    }
    @Override public String getClusterType() {
        return clusterType;
    }
}
