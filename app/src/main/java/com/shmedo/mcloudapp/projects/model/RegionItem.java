package com.shmedo.mcloudapp.projects.model;

import com.amap.api.maps.model.LatLng;
import com.shmedo.mcloudapp.projects.cluster.ClusterItem;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public class RegionItem implements ClusterItem {
    private LatLng mLatLng;
    private String mTitle;

    public RegionItem(LatLng latLng, String title) {
        mLatLng=latLng;
        mTitle=title;
    }

    @Override
    public LatLng getPosition() {

        return mLatLng;
    }
    public String getTitle(){
        return mTitle;
    }

}
