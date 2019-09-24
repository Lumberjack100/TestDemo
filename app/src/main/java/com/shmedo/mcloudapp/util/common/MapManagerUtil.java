package com.shmedo.mcloudapp.util.common;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.shmedo.mcloudapp.R;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.common
 * 文件名:   MapManagerUtil
 * 创建者:   dpc
 * 创建时间:  2019/1/23 08:59
 * 描述：      地图封装页面
 */
public class MapManagerUtil {

    public static void initMarker(AMap aMap, Context  context){
        LatLng latLng = new LatLng(31.0964540159,121.591186523);
        LatLng latLng2 = new LatLng(31.1718317,121.65444374);
        addMarkerToMap(aMap,latLng,"das","DAS");
        addMarkerToMap(aMap,latLng2,"上海","上海市浦东新区");


    }

    //添加marker
    public static void addMarkerToMap(AMap aMap,LatLng latLng, String title, String snippet) {
        aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
            .position(latLng)
            .title(title)
            .snippet(snippet)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker))
        );
    }
}
