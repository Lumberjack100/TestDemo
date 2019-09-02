package com.shmedo.mcloudapp.entity.cluster;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.cluster
 * 文件名:   Cluster
 * 创建者:   dpc
 * 创建时间:  2019/8/14 11:29
 * 描述：    TODO
 */
public class Cluster {

    private LatLng mLatLng;
    private List<ClusterItem> mClusterItems;
    private Marker mMarker;


    public Cluster(LatLng latLng) {

        mLatLng = latLng;
        mClusterItems = new ArrayList<ClusterItem>();
    }

    public void addClusterItem(ClusterItem clusterItem) {
        mClusterItems.add(clusterItem);
    }

    public int getClusterCount() {
        return mClusterItems.size();
    }



    public LatLng getCenterLatLng() {
        return mLatLng;
    }

    public void setMarker(Marker marker) {
        mMarker = marker;
    }

    public Marker getMarker() {
        return mMarker;
    }

    public List<ClusterItem> getClusterItems() {
        return mClusterItems;
    }
}
