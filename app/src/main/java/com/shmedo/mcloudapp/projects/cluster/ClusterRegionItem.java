package com.shmedo.mcloudapp.projects.cluster;

import com.amap.api.maps.model.LatLng;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public class ClusterRegionItem implements ClusterItem {
    private LatLng mLatLng;
    private String mTitle;

    public ClusterRegionItem(LatLng latLng, String title) {
        mLatLng = latLng;
        mTitle = title;
    }

    @Override
    public LatLng getPosition() {

        return mLatLng;
    }

    public String getTitle() {
        return mTitle;
    }

}
